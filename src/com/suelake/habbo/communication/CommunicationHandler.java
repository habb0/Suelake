package com.suelake.habbo.communication;

import java.util.Vector;

import com.blunk.Log;
import com.blunk.security.FuseSecret;
import com.blunk.security.RC4;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.access.UserAccessEntry;
import com.suelake.habbo.items.ItemInventoryHandler;
import com.suelake.habbo.items.ItemTradeHandler;
import com.suelake.habbo.messenger.MessengerComponent;
import com.suelake.habbo.moderation.CallForHelp;
import com.suelake.habbo.moderation.ModerationBan;
import com.suelake.habbo.net.InfoConnection;
import com.suelake.habbo.spaces.instances.SpaceInstance;
import com.suelake.habbo.users.User;
import com.suelake.habbo.util.RoomDirectoryEncoding;

public class CommunicationHandler
{
	// Client
	public final int clientID;
	private ClientManager m_clientManager;
	private InfoConnection m_connection;
	private RC4 m_crypto;
	public String keyEncrypted;
	
	// Network messaging
	public ServerMessage response;
	private long m_lastMessageTime;
	private ClientRequestHandlerManager m_requestManager;
	
	// User & access
	private User m_userObject;
	private UserAccessEntry m_accessEntry;
	
	// Session variables
	private MessengerComponent m_messenger;
	private SpaceInstance m_spaceInstance;
	private ItemInventoryHandler m_itemInventory;
	private ItemTradeHandler m_itemTrader;
	
	/**
	 * The database ID of the flat this client passed the access check for.
	 */
	public int authenticatedFlat;
	/**
	 * True if this client is waiting for a doorbell response at entering a flat.
	 */
	public boolean waitingForFlatDoorbell;
	/**
	 * The database ID of the teleporter item this client is using to enter a flat.
	 */
	public int authenticatedTeleporter;
	
	public CommunicationHandler(int clientID, InfoConnection connection, ClientManager clientMgr)
	{
		this.clientID = clientID;
		m_connection = connection;
		m_clientManager = clientMgr;
	}
	
	public void handleIncomingNetworkData(int msgType, char[] msgData)
	{
		// Decipher data?
		if (m_crypto != null)
		{
			msgData = m_crypto.decipher(msgData);
		}
		
		// 256 = enterprise server
		// 257 = room server
		// 258 = room directory
		String serverType = null;
		if (msgType == 256)
		{
			serverType = "ENTERPRISE";
		}
		else if (msgType == 257)
		{
			serverType = "ROOM";
		}
		else if (msgType == 258)
		{
			serverType = "ROOM_DIRECTORY";
		}
		
		// Enterprise server
		if (msgType == 256 || msgType == 257)
		{
			// Filter out '#', it's EOF for server>client messages (security issue)
			for (int i = 0; i < msgData.length; i++)
			{
				if (msgData[i] == '#')
				{
					msgData[i] = '?';
				}
			}
			
			// Try to parse ClientMessage object
			ClientMessage msg = ClientMessage.parse(msgData);
			
			// Could we parse the message? If so, this is a valid client message and enc is OK! :-D
			if (msg != null)
			{
				// Log message
				Log.debug("Client [" + this.clientID + "][@" + serverType + "] <-- " + msg.toString());
				
				// Handle request
				m_lastMessageTime = TimeHelper.getTime();
				m_requestManager.handleRequest(msg);
			}
		}
		else if (msgType == 258) // ROOM_DIRECTORY
		{
			this.roomDirectory(msgData);
		}
		else
		{
			Log.info("Uncaught message type [" + msgType + "] for client " + this.clientID);
		}
	}
	
	public void start()
	{
		// Install components
		m_requestManager = new ClientRequestHandlerManager(this);
		m_messenger = new MessengerComponent(this);
		m_itemInventory = new ItemInventoryHandler(this);
		m_itemTrader = new ItemTradeHandler(this);
		
		// Register the handlers for security (crypto init etc)
		m_requestManager.registerSecurityHandlers(true);
		
		// Initialize response object (this one is reused all the time)
		this.response = new ServerMessage();
		
		// Start network thread so we can start reading data
		m_connection.startThread();
	}
	
	public void stop(String reason)
	{
		// Destroy network connection
		try
		{
			m_connection.stop();
		}
		catch (Exception ex)
		{
			Log.error("Client #" + this.clientID + ": error stopping network connection.", ex);
		}
		
		// Perform session cleanup
		try
		{
			// Leave space
			this.leaveSpace();
			
			// Logged in?
			if (m_userObject != null)
			{
				// Update last activity
				m_userObject.updateLastActivity();
				
				// Update access log
				if (m_accessEntry != null)
				{
					HabboHotel.getAccessControl().logLogout(m_accessEntry);
				}
				
				// Destroy MUS connection
				HabboHotel.getPhotoService().getServer().killClientOfUser(m_userObject.ID);
				
				// Update user object
				HabboHotel.getUserRegister().updateUser(m_userObject);
			}
		}
		catch (Exception ex)
		{
			Log.error("Client #" + this.clientID + ": error performing session cleanup operations", ex);
		}
		finally
		{
			// Release client
			m_clientManager.removeClient(this, reason);
		}
	}
	
	/**
	 * Tries to send the current ServerMessage (response) in the CommunicationHandler to the underlying TcpConnection.
	 * 
	 * @see sendMessage(ServerMessage)
	 */
	public void sendResponse()
	{
		sendMessage(this.response);
	}
	
	/**
	 * Tries to send a given ServerMessage object to the underlying TcpConnection.
	 * 
	 * @param msg The ServerMessage object representing the message to send.
	 */
	public void sendMessage(ServerMessage msg)
	{
		if (msg != null && m_connection != null)
		{
			String result = msg.getResult();
			Log.debug("Client [" + this.clientID + "][] --> " + result);
			m_connection.sendData(result);
		}
	}
	
	public void cryptoInit()
	{
		// Secret stuff
		String publicKey = FuseSecret.GenerateSecret();
		int decodedKey = FuseSecret.SecretDecode(publicKey);
		
		// Set encryption serverside
		m_crypto = new RC4(decodedKey);
		this.keyEncrypted = Integer.toString(decodedKey);
		
		// Transmit key
		response.set("SECRET_KEY");
		response.appendArgument(publicKey.toString());
		sendResponse();
	}
	
	public void cryptoOK()
	{
		// Register handlers
		m_requestManager.registerSecurityHandlers(false);
		m_requestManager.registerGlobalHandlers(true);
		m_requestManager.registerPreLoginHandlers(true);
	}
	
	public void systemMsg(String text)
	{
		ServerMessage msg = new ServerMessage("SYSTEMBROADCAST");
		msg.appendArgument(text);
		
		this.sendMessage(msg);
	}
	
	public void systemError(String text)
	{
		ServerMessage msg = new ServerMessage("ERROR");
		msg.appendArgument(text);
		
		this.sendMessage(msg);
	}
	
	public void moderatorWarning(String text)
	{
		ServerMessage msg = new ServerMessage("ERROR");
		msg.appendArgument("MODERATOR WARNING");
		msg.appendArgument(text, '/');
		
		this.sendMessage(msg);
	}
	
	public void sendBan(ModerationBan ban)
	{
		ServerMessage msg = new ServerMessage();
		
		// Send reason
		msg.set("ERROR");
		msg.appendArgument("MODERATOR WARNING/");
		msg.append(ban.generateReport());
		String appealEmail = HabboHotel.getPropBox().get("config.moderation.bans.appealemail", "admin@localhost");
		msg.appendNewArgument("");
		msg.appendNewArgument("Please use this information if you would like to appeal your ban.\rYou can appeal a ban by sending an email to " + appealEmail);
		this.sendMessage(msg);
		
		// Send blank ban message to prevent client from graying out
		msg.set("USERBANNED");
		this.sendMessage(msg);
	}
	
	public void login(String name, String password)
	{
		User usr = HabboHotel.getUserRegister().getUserInfo(name, false);
		if (usr == null)
		{
			this.systemError("login incorrect: Wrong username");
		}
		else if (!usr.password.equals(password))
		{
			this.systemError("login incorrect: Wrong password");
		}
		else
		{
			// Check if user is banned
			ModerationBan ban = HabboHotel.getModerationCenter().getUserBan(usr.ID);
			if (ban != null)
			{
				this.sendBan(ban);
				this.stop("user is banned");
			}
			else
			{
				// Disconnect user if already logged in on other connection
				HabboHotel.getGameClients().disconnectUser(usr.ID, "concurrent login");
				
				// Create access log entry
				m_accessEntry = HabboHotel.getAccessControl().newUserAccessEntry();
				if (m_accessEntry != null)
				{
					m_accessEntry.userID = usr.ID;
					m_accessEntry.ip = this.getConnection().getIpAddress();
					HabboHotel.getAccessControl().logLogin(m_accessEntry);
				}
				
				// Set user object
				m_userObject = usr;
				
				// Re-calculate HC etc
				if (usr.calculateHC(false))
				{
					HabboHotel.getUserRegister().updateUser(usr);
				}
				
				// Handle handlers
				m_requestManager.registerPreLoginHandlers(false);
				m_requestManager.registerUserHandlers(true);
				m_requestManager.registerNavigatorHandlers(true);
				
				// Is this user a moderation team member?
				if (usr.hasRight("can_answer_cfh"))
				{
					// Register moderation handlers
					m_requestManager.registerModerationHandlers(true);
					
					// Send all the pending calls (while this moderator was offline)
					Vector<CallForHelp> pendingCalls = HabboHotel.getModerationCenter().getPendingCalls();
					for (CallForHelp call : pendingCalls)
					{
						response.set("CRYFORHELP");
						response.appendObject(call);
						sendResponse();
					}
				}
				
				// Send user rights
				response.set("U_RTS");
				for (String right : HabboHotel.getAccessControl().getUserRightsForRole(usr.role))
				{
					response.appendNewArgument(right);
				}
				sendResponse();
				
				// Login OK!
				response.set("OK");
				sendResponse();
				
				// Send amount of films for camera this User has
				this.sendFilm();
				
				// Send message of the day
				if (HabboHotel.getAccessControl().getMessageOfTheDay() != null)
				{
					this.systemMsg("Message Of The Day:\r" + HabboHotel.getAccessControl().getMessageOfTheDay());
				}
				
				// Log this event
				Log.info("User #" + usr.ID + " [" + usr.name + "] logged in from client #" + this.clientID + " [" + m_connection.getIpAddress() + "]");
			}
		}
	}
	
	public void roomDirectory(char[] data)
	{
		// Decode data
		int[] decoded = RoomDirectoryEncoding.decode(data);
		boolean isFlat = (decoded[0] == 128);
		int spaceID = decoded[1];
		int doorID = decoded[2];
		
		Log.debug("Client [" + this.clientID + "][@ROOM_DIRECTORY] <-- space ID: " + spaceID + ", door ID: " + doorID);
		
		// ROOM_DIRECTORY successfully received data!
		response.set("OPC_OK");
		sendResponse();
		
		// If this space is not a flat, we can skip all access check handlers etc and go to space immediately
		if (!isFlat)
		{
			this.goToSpace(spaceID);
		}
	}
	
	public void goToSpace(int spaceID)
	{
		// Leave space (if any)
		this.leaveSpace();
		
		// Get instance of space
		SpaceInstance instance = HabboHotel.getSpaceDirectory().getInstance(spaceID, true);
		if (instance == null)
		{
			// Space does not exist or could not create instance
			this.kickFromSpace("Could not enter space " + spaceID + "!\rSpace does not exist or some technical malfunctioning is going on!\r*whips server hamsters*");
			return;
		}
		
		// Can the space hold more users?
		if (this.authenticatedTeleporter == 0 && instance.isFull())
		{
			// Is this user the owner of the space?
			if (this.getUserObject().ID != instance.getInfo().ownerID)
			{
				this.kickFromSpace("Space is full.");
				return;
			}
		}
		
		// Attempt to register client with space instance
		if (this.waitingForFlatDoorbell)
		{
			this.waitingForFlatDoorbell = false;
		}
		else
		{
			if (this.authenticatedTeleporter == 0 && !instance.registerClient(this.clientID))
			{
				this.kickFromSpace("Could not enter space (failed to register client) for whatever reason.\rPlease retry.");
				return;
			}
		}
		
		// Register appropriate request handlers
		m_requestManager.registerSpaceHandlers(true);
		
		// User flat?
		if (instance.getInfo().isUserFlat())
		{
			m_requestManager.registerUserFlatHandlers(true);
		}
		else
		{
			if (instance.getModel().hasSwimmingPool)
			{
				// Register request handlers for swimming pools
				m_requestManager.registerSwimmingPoolHandlers(true);
			}
		}
		
		// Set instance field
		m_spaceInstance = instance;
		
		// Notify client that room instance is ready
		response.set("ROOM_READY");
		response.appendArgument(instance.getModel().type);
		response.appendArgument(Integer.toString(instance.getInfo().ID));
		sendResponse();
		
		// Flat?
		if (instance.getInfo().isUserFlat())
		{
			response.set("FLATPROPERTY");
			response.appendArgument("wallpaper");
			response.appendPartArgument(Integer.toString(instance.getInfo().wallpaper));
			sendResponse();
			
			response.set("FLATPROPERTY");
			response.appendArgument("floor");
			response.appendPartArgument(Integer.toString(instance.getInfo().floor));
			sendResponse();
		}
		else
		{
			if (instance.getModel().hasSwimmingPool)
			{
				// Send amount of game tickets this User has
				this.sendGameTickets();
			}
		}
	}
	
	public void leaveSpace()
	{
		// Reset space IDs
		if (this.authenticatedTeleporter == 0)
		{
			this.authenticatedFlat = 0;
		}
		
		// Leave from space instance
		if (m_spaceInstance != null)
		{
			// Optionally stop trading
			this.stopTrading();
			
			// Clear item inventory
			m_itemInventory.clear();
			
			// De-register common space handlers
			m_requestManager.registerSpaceHandlers(false);
			
			// User flat?
			if (m_spaceInstance.getInfo().isUserFlat())
			{
				m_requestManager.registerUserFlatHandlers(false);
			}
			else
			{
				// Space has swimming pool?
				if (m_spaceInstance.getModel().hasSwimmingPool)
				{
					m_requestManager.registerSwimmingPoolHandlers(false);
				}
			}
			
			// Leave instance
			m_spaceInstance.deRegisterClient(this.clientID);
			m_spaceInstance = null;
		}
	}
	
	public void kickFromSpace(String info)
	{
		// Leave space instance
		this.leaveSpace();
		
		// Clear client stage
		ServerMessage msg = new ServerMessage("CLC");
		this.sendMessage(msg);
		
		// Optionally: send moderation message
		if (info != null)
		{
			this.moderatorWarning(info);
		}
	}
	
	public void sendFilm()
	{
		if (m_userObject != null)
		{
			ServerMessage msg = new ServerMessage("FILM");
			msg.appendArgument(Short.toString(m_userObject.film));
			this.sendMessage(msg);
		}
	}
	
	public void sendCredits()
	{
		if (m_userObject != null)
		{
			ServerMessage msg = new ServerMessage("WALLETBALANCE");
			msg.appendArgument(Short.toString(m_userObject.credits));
			this.sendMessage(msg);
		}
	}
	
	public void sendGameTickets()
	{
		if (m_userObject != null)
		{
			ServerMessage msg = new ServerMessage("PH_TICKETS");
			msg.appendArgument(Short.toString(m_userObject.gameTickets));
			this.sendMessage(msg);
		}
	}
	
	/**
	 * Stops the item trade in the ItemTradeHandlers of both trading partners.
	 */
	public void stopTrading()
	{
		// Trading?
		if (m_itemTrader.busy())
		{
			// Close trade handlers for both clients
			m_itemTrader.getPartner().close();
			m_itemTrader.close();
		}
	}
	
	public InfoConnection getConnection()
	{
		return m_connection;
	}
	
	public long getLastMessageTime()
	{
		return m_lastMessageTime;
	}
	
	public ClientManager getClientManager()
	{
		return m_clientManager;
	}
	
	public ClientRequestHandlerManager getRequestHandlers()
	{
		return m_requestManager;
	}
	
	public User getUserObject()
	{
		return m_userObject;
	}
	
	public UserAccessEntry getAccessEntry()
	{
		return m_accessEntry;
	}
	
	public MessengerComponent getMessenger()
	{
		return m_messenger;
	}
	
	public SpaceInstance getSpaceInstance()
	{
		return m_spaceInstance;
	}
	
	public ItemInventoryHandler getItemInventory()
	{
		m_itemInventory.loadStripItems();
		return m_itemInventory;
	}
	
	public ItemTradeHandler getItemTrader()
	{
		return m_itemTrader;
	}
}
