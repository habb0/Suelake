package com.suelake.habbo.spaces.instances;

import java.util.Vector;

import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.games.WobbleSquabbleHandler;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.spaces.SpaceModel;
import com.suelake.habbo.spaces.pathfinding.RotationCalculator;
import com.suelake.habbo.util.ChatUtility;

/**
 * Represents an instance of a space running inside a SpaceInstanceServer. Broadcasts data of all action to inside SpaceUsers etc.
 * 
 * @author Nillus
 */
public class SpaceInstance
{
	/**
	 * The Space object holding all the data of this space, such as ID, name, type etc.
	 */
	private Space m_info;
	private SpaceModel m_model;
	
	private Object m_lock = new Object();
	private SpaceInstanceInteractor m_interactor;
	private Vector<Integer> m_enteringClients;
	private Vector<SpaceUser> m_users;
	
	private Vector<SpaceBot> m_bots;
	private WobbleSquabbleHandler m_handlerWobbleSquabble;
	
	private Vector<Integer> m_flatControllers;
	
	public SpaceInstance(Space info, SpaceModel model)
	{
		// Set data
		m_info = info;
		m_model = model;
		
		// Install interactor
		m_interactor = new SpaceInstanceInteractor(this);
		
		// Create collections with initial capacities
		m_enteringClients = new Vector<Integer>(2);
		m_users = new Vector<SpaceUser>(m_info.usersMax / 5);
		m_bots = new Vector<SpaceBot>(0);
		m_flatControllers = new Vector<Integer>(2);
		
		// Install wobble squabble?
		if (model.type.equals("md_a"))
		{
			m_handlerWobbleSquabble = new WobbleSquabbleHandler(this);
		}
	}
	
	/**
	 * Stops and destroys the interactor, removes all the bots and users and stops the SpaceInstance.
	 */
	public void destroy()
	{
		// Stop and clear interactor
		m_interactor.stop();
		m_interactor.clear();
		
		// Stop wobble squabble
		if (m_handlerWobbleSquabble != null)
		{
			m_handlerWobbleSquabble.stop();
		}
		
		// Clear the user collections
		synchronized (m_lock)
		{
			m_enteringClients.clear();
			
			// Force removal of all users
			for (SpaceUser usr : m_users)
			{
				usr.getCommunicator().kickFromSpace(null);
			}
			m_users.clear();
			
			// Remove bots
			for (SpaceUser bot : m_bots)
			{
				
			}
			m_bots.clear();
			
			// Clear flatcontrollers
			m_flatControllers.clear();
		}
	}
	
	/**
	 * Broadcasts a string of characters to all users in the space. (network)
	 * 
	 * @param data The data string to broadcast.
	 */
	public void broadcast(String data)
	{
		Log.debug("Space [" + m_info.ID + "] --> " + data);
		synchronized (m_lock)
		{
			for (SpaceUser usr : m_users)
			{
				usr.getCommunicator().getConnection().sendData(data);
			}
		}
	}
	
	/**
	 * Broadcasts a ServerMessage object to all users in the space.
	 * 
	 * @param msg The ServerMessage object to broadcast.
	 */
	public void broadcast(ServerMessage msg)
	{
		this.broadcast(msg.getResult());
	}
	
	/**
	 * Attempts to register a client ID with the space instance.
	 * 
	 * @param clientID The client ID to register.
	 * @return True if registering succeeded, False otherwise.
	 */
	public boolean registerClient(int clientID)
	{
		synchronized (m_lock)
		{
			if (!m_enteringClients.contains(clientID))
			{
				m_enteringClients.addElement(clientID);
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	/**
	 * Attempts to de-register a client from the space instance, removes it from collection etc.
	 * 
	 * @param clientID The ID of the client to de-register.
	 */
	public void deRegisterClient(int clientID)
	{
		SpaceUser usr = this.getUserByClientID(clientID);
		if (usr == null) // User not started?
		{
			// Client was just entering space, nothing more
			synchronized (m_lock)
			{
				m_enteringClients.removeElement(clientID);
			}
		}
		else
		{
			// Remove user
			synchronized (m_lock)
			{
				m_users.removeElement(usr);
			}
			
			// Log activity
			this.updateUserAmount();
			
			// Are there remaining users in the space?
			if (this.userAmount() > 0)
			{
				// Broadcast 'remove from room'
				ServerMessage msg = new ServerMessage("LOGOUT");
				msg.appendArgument(usr.getUserObject().name);
				this.broadcast(msg);
				
				// Release map spot
				this.getInteractor().setUserMapTile(usr.X, usr.Y, false);
				
				// Update users amount
			}
			else
			{
				// Destroy instance
				HabboHotel.getSpaceDirectory().destroyInstance(m_info.ID);
			}
		}
	}
	
	public void reloadFlatControllers()
	{
		synchronized (m_lock)
		{
			m_flatControllers = HabboHotel.getSpaceAdmin().getFlatControllersForSpace(m_info.ID);
		}
	}
	
	public boolean addFlatController(int userID)
	{
		// Not a flat controller already?
		synchronized (m_lock)
		{
			if (m_flatControllers.contains(userID))
			{
				return false;
			}
			else
			{
				// Add to collection
				m_flatControllers.add(userID);
				
				// Update in database
				HabboHotel.getSpaceAdmin().addFlatControllerForSpace(m_info.ID, userID);
				
				// Added!
				return true;
			}
		}
	}
	
	public boolean removeFlatController(int userID)
	{
		// Really a flat controller?
		synchronized (m_lock)
		{
			if (!m_flatControllers.contains(userID))
			{
				return false;
			}
			else
			{
				// Remove from collection
				m_flatControllers.add(userID);
				
				// Update in database
				HabboHotel.getSpaceAdmin().removeFlatControllerForSpace(m_info.ID, userID);
				
				// Removed!
				return true;
			}
		}
	}
	
	public boolean isFlatController(int userID)
	{
		synchronized (m_lock)
		{
			return m_flatControllers.contains(userID);
		}
	}
	
	/**
	 * Creates and spawns a SpaceUser of a given client (that has registered at the space prior to calling this method) in the SpaceInstance.
	 * 
	 * @param client The CommunicationHandler representing the client.
	 */
	public boolean activateUser(CommunicationHandler client)
	{
		// Has this client registered with the SpaceInstance prior to activating it's user?
		if (!m_enteringClients.contains(client.clientID))
		{
			return false;
		}
		
		// Create SpaceUser object
		SpaceUser usr = new SpaceUser(client);
		
		// Locate user at spawn position
		if (client.authenticatedTeleporter == 0)
		{
			usr.X = m_model.doorX;
			usr.Y = m_model.doorY;
			usr.Z = m_model.doorZ;
		}
		else
		{
			Item obj = this.getInteractor().getActiveObject(client.authenticatedTeleporter);
			if (obj == null)
			{
				return false;
			}
			else
			{
				// Spawn in teleporter
				usr.X = obj.X;
				usr.Y = obj.Y;
				usr.Z = obj.Z;
				client.authenticatedTeleporter = 0;
				
				// Broadcast activity
				this.getInteractor().broadcastTeleporterActivity(obj.ID, obj.definition.sprite, usr.getUserObject().name, false);
			}
		}
		
		// Determine privileges if in user flat
		if (this.getInfo().isUserFlat())
		{
			boolean isAnyFlatController = client.getUserObject().hasRight("is_any_flatcontroller");
			usr.isFlatOwner = ((client.getUserObject().ID == this.getInfo().ownerID) || isAnyFlatController);
			usr.isFlatController = (usr.isFlatOwner || this.getInfo().superUsers || m_flatControllers.contains(client.getUserObject().ID));
			usr.refreshFlatPrivileges();
			
			if (isAnyFlatController)
			{
				client.sendMessage(new ServerMessage("YOUAREMOD"));
			}
		}
		
		// Set badge if available
		if (client.getUserObject().badge != null)
		{
			usr.addStatus("mod", client.getUserObject().badge, 0, null, 0, 0);
		}
		
		// Add user officially to room
		synchronized (m_lock)
		{
			m_enteringClients.removeElement(client.clientID);
			m_users.addElement(usr);
		}
		
		// Broadcast entry of user
		ServerMessage msg = new ServerMessage("USERS");
		msg.appendObject(usr);
		this.broadcast(msg);
		
		// Update user amount
		this.updateUserAmount();
		
		// Activated!
		return true;
	}
	
	public void addBot(SpaceBot bot)
	{
		if (bot != null)
		{
			// Position bot
			if (!this.getInteractor().mapTileExists(bot.X, bot.Y))
			{
				bot.X = m_model.doorX;
				bot.Y = m_model.doorY;
			}
			bot.Z = m_interactor.getMapTileHeight(bot.X, bot.Y);
			
			// Add bot to collection
			synchronized (m_lock)
			{
				m_bots.add(bot);
			}
			
			// Broadcast entry to room
			ServerMessage msg = new ServerMessage("USERS");
			msg.appendObject(bot);
			this.broadcast(msg);
			
			// Move 1 step
			m_interactor.startUserMovement(bot, bot.X, (short)(bot.Y + 1), false);
			
			// Log
			Log.info("SpaceInstance [" + m_info.ID + "]: loaded bot #" + bot.getUserObject().ID + " [" + bot.getUserObject().name + "]");
		}
	}
	
	public void loadBots()
	{
		// Get information for bots
		Vector<SpaceBot> bots = HabboHotel.getSpaceAdmin().getBotsForSpace(m_info.ID);
		
		// Cycle all bots
		synchronized (m_lock)
		{
			for (SpaceBot bot : bots)
			{
				// Set default AI
				bot.setMode(SpaceBotMode.AI_DEFAULT);
				
				// Put bot in space
				this.addBot(bot);
			}
		}
	}
	
	public void clearBots()
	{
		synchronized (m_lock)
		{
			for (SpaceBot bot : m_bots)
			{
				// Broadcast 'remove from room'
				ServerMessage msg = new ServerMessage("LOGOUT");
				msg.appendArgument(bot.getUserObject().name);
				this.broadcast(msg);
				
				// Release map spot
				this.getInteractor().setUserMapTile(bot.X, bot.Y, false);
			}
			
			// Be gone!
			m_bots.clear();
		}
	}
	
	public void updateUserAmount()
	{
		this.getInfo().usersNow = (short)this.userAmount();
		HabboHotel.getSpaceAdmin().updateSpaceInfo(this.getInfo());
	}
	
	/**
	 * Finds a SpaceUser with a given client ID and returns it.
	 * 
	 * @param clientID The ID of the client to get the SpaceUser of.
	 * @return The SpaceUser if the user is found, null otherwise.
	 */
	public SpaceUser getUserByClientID(int clientID)
	{
		synchronized (m_lock)
		{
			for (SpaceUser usr : m_users)
			{
				if (usr.getCommunicator().clientID == clientID)
				{
					return usr;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Finds a SpaceUser with a given name and returns it.
	 * 
	 * @param name The name string of the user to find. Case sensitive.
	 * @return The SpaceUser if the user is found, null otherwise.
	 */
	public SpaceUser getUserByName(String name)
	{
		synchronized (m_lock)
		{
			for (SpaceUser usr : m_users)
			{
				if (usr.getUserObject().name.equals(name))
				{
					return usr;
				}
			}
		}
		
		return null;
	}
	
	public SpaceUser getUserOnTile(short X, short Y)
	{
		synchronized (m_lock)
		{
			for (SpaceBot bot : m_bots)
			{
				if (bot.X == X && bot.Y == Y)
				{
					return bot;
				}
			}
			
			for (SpaceUser usr : m_users)
			{
				if (usr.X == X && usr.Y == Y)
				{
					return usr;
				}
			}
		}
		
		return null;
	}
	
	public void chat(SpaceUser src, String text, boolean shout)
	{
		// Split the words
		String[] words = text.split(" ");
		
		// Filter the words
		ChatUtility.filterWords(words);
		
		// Detect the first emote in the sentence, and apply it to the source user
		String emote = ChatUtility.detectEmote(words);
		if (emote != null)
		{
			src.addStatus("gest", emote, 5, null, 0, 0);
		}
		
		// Determine length of talk animation duration, but not if just saying ":)" etc
		if (emote == null || words.length > 1)
		{
			int talkDuration = 1;
			if (words.length > 1)
			{
				if(words.length >= 10)
				{
					talkDuration = 5;
				}
				else
				{
					talkDuration = words.length / 2;
				}
			}
			
			// Set talk animation status
			src.addStatus("talk", null, talkDuration, null, 0, 0);
		}
		
		// Build the shout message when shouting, we won't need to compute new ones then
		ServerMessage msgFull = new ServerMessage();
		msgFull.set(shout ? "SHOUT" : "CHAT");
		msgFull.appendArgument(src.getUserObject().name);
		msgFull.appendArgument(text);
		if (!src.isBot())
		{
			src.getCommunicator().sendMessage(msgFull);
		}
		
		// Reserve a variable for the garbled message
		ServerMessage msgGarbled = null;
		
		// Go through all the bots, they don't hear anything, but they pretend they do though!
		synchronized (m_bots)
		{
			for (SpaceBot bot : m_bots)
			{
				// Skip itself
				if (bot == src)
				{
					continue;
				}
				
				// Calculate distance between this bot and the source of chat
				double distanceToSource = RotationCalculator.calculateDistance(bot.X, bot.Y, src.X, src.Y);
				
				// Does this bot hear anything at all?
				if (distanceToSource <= ChatUtility.HEARING_RADIUS_MAX)
				{
					// Atleast hearing something, attempt to rotate head to source
					bot.angleHeadTo(src.X, src.Y);
					
					// TODO: bot interaction?
				}
			}
		}
		
		// Go through all the users
		synchronized (m_users)
		{
			for (SpaceUser usr : m_users)
			{
				// Skip itself
				if (usr == src)
				{
					continue;
				}
				
				// Calculate distance between this user and the source of chat
				double distanceToSource = RotationCalculator.calculateDistance(usr.X, usr.Y, src.X, src.Y);
				boolean withinMaxRadius = (distanceToSource <= ChatUtility.HEARING_RADIUS_MAX);
				
				// Does this user hear anything at all?
				if (shout || withinMaxRadius)
				{
					// If within the max radius, then attempt to rotate head to source. User atleast hears 'something'
					if(withinMaxRadius)
					{
						usr.angleHeadTo(src.X, src.Y);
					}
					
					// Is this user hearing the full message?
					if (shout || distanceToSource <= ChatUtility.HEARING_RADIUS_NORM)
					{
						usr.getCommunicator().sendMessage(msgFull);
					}
					else
					{
						// Build a garbled message if not done before (it's the same for anyone!)
						if (msgGarbled == null)
						{
							msgGarbled = new ServerMessage("CHAT");
							msgGarbled.appendArgument(src.getUserObject().name);
							msgGarbled.appendArgument(ChatUtility.garbleChat(text));
						}
						usr.getCommunicator().sendMessage(msgGarbled);
					}
				}
			}
		}
	}
	
	/**
	 * Notifies all flat controllers in the room that a user is ringing the doorbell to gain access to this user flat.
	 * 
	 * @param name The username of the user that rings the doorbell.
	 * @return True if atleast one flat controller is in the flat and has 'heard' the doorbell, False otherwise.
	 */
	public boolean ringDoorbell(String name)
	{
		boolean isReceived = false;
		synchronized (m_lock)
		{
			for (SpaceUser usr : m_users)
			{
				// Can this user answer doorbelling users?
				if (usr.isFlatController)
				{
					// Broadcast 'User %x rings doorbell. Let in?'
					ServerMessage msg = new ServerMessage("DOORBELL_RINGING");
					msg.appendArgument(name);
					usr.getCommunicator().sendMessage(msg);
					
					// Atleast one user has heard the doorbell
					isReceived = true;
				}
			}
		}
		
		return isReceived;
	}
	
	/**
	 * Lets a doorbelling user with a given name enter the flat.
	 * 
	 * @param clientID The client ID of the client that answers the doorbell.
	 * @param name The name of the user that is let in.
	 */
	public void answerDoorbell(int clientID, String name)
	{
		// Can this user answer the doorbell?
		if (this.getUserByClientID(clientID).isFlatController)
		{
			synchronized (m_lock)
			{
				for (int enteringClientID : m_enteringClients)
				{
					// Is this the user that gets a doorbell answer?
					CommunicationHandler client = HabboHotel.getGameClients().getClient(enteringClientID);
					if (client != null && client.waitingForFlatDoorbell && client.getUserObject().name.equals(name))
					{
						// Authenticate client with flat
						client.authenticatedFlat = this.getInfo().ID;
						
						// Trigger client to enter
						client.sendMessage(new ServerMessage("FLAT_LETIN"));
						return;
					}
				}
			}
		}
	}
	
	public void moderationKick(byte issuerRole, String info)
	{
		// Maintain collection with targets
		Vector<SpaceUser> toKick = new Vector<SpaceUser>();
		
		// Check all the users and grab the targets
		synchronized (m_lock)
		{
			for (SpaceUser usr : m_users)
			{
				// Safe to kick?
				if (usr.getUserObject().role < issuerRole)
				{
					toKick.add(usr);
				}
			}
		}
		
		// Kick the targets
		for (SpaceUser usr : toKick)
		{
			usr.getCommunicator().kickFromSpace(info);
		}
	}
	
	public void showProgram(String program, String data)
	{
		ServerMessage msg = new ServerMessage("SHOWPROGRAM");
		msg.appendArgument(program);
		msg.appendArgument(data);
		
		this.broadcast(msg);
	}
	
	/**
	 * Returns the Space object of this space instance, holding all data about the space etc.
	 * 
	 * @return
	 */
	public Space getInfo()
	{
		return m_info;
	}
	
	/**
	 * Returns the SpaceModel of this space.
	 */
	public SpaceModel getModel()
	{
		return m_model;
	}
	
	/**
	 * Returns the SpaceInstanceInteractor instance of this space instance.
	 */
	public SpaceInstanceInteractor getInteractor()
	{
		return m_interactor;
	}
	
	/**
	 * Returns the SpaceUsers.
	 */
	public Vector<SpaceUser> getUsers()
	{
		return m_users;
	}
	
	/**
	 * Returns the SpaceUser bots.
	 */
	public Vector<SpaceBot> getBots()
	{
		return m_bots;
	}
	
	/**
	 * Returns the amount of users in this space instance.
	 */
	public int userAmount()
	{
		synchronized (m_lock)
		{
			return (m_users != null) ? m_users.size() : 0;
		}
	}
	
	/**
	 * Returns True if this space instance cannot hold more users, False otherwise.
	 */
	public boolean isFull()
	{
		return this.userAmount() >= m_info.usersMax;
	}
	
	public Vector<Integer> getFlatControllers()
	{
		return m_flatControllers;
	}
	
	public WobbleSquabbleHandler getWobbleSquabbleHandler()
	{
		return m_handlerWobbleSquabble;
	}
}
