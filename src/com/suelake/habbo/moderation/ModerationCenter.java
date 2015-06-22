package com.suelake.habbo.moderation;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.blunk.Environment;
import com.blunk.Log;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.access.UserAccessEntry;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;

/**
 * ModerationCenter is where authorized Users perform their moderation tasks such as banning and kicking Users and replying Call for Helps.
 * 
 * @author Nillus
 */
public class ModerationCenter
{
	@SuppressWarnings("unchecked")
	private Class m_moderationBanClass;
	
	private int m_callCounter;
	private Vector<CallForHelp> m_pendingCalls;
	
	public ModerationCenter()
	{
		ModerationBan sample = (ModerationBan)HabboHotel.getDataObjectFactory().newObject("ModerationBan");
		if (sample != null)
		{
			m_moderationBanClass = sample.getClass();
		}
		
		m_callCounter = 0;
		m_pendingCalls = new Vector<CallForHelp>();
	}
	
	public ModerationBan getBan(int ID)
	{
		ModerationBan ban = this.newModerationBan();
		ban.ID = ID;
		
		// Load and validate the ban
		if (Environment.getDatabase().load(ban))
		{
			if (this.banIsValid(ban))
			{
				return ban;
			}
		}
		
		// Not found / expired
		return null;
	}
	
	public ModerationBan getIpBan(String ip)
	{
		ModerationBan ban = this.newModerationBan();
		ban.ip = ip;
		
		// Load and validate the ban
		if (Environment.getDatabase().load(ban))
		{
			if (this.banIsValid(ban))
			{
				return ban;
			}
		}
		
		// Not found / expired
		return null;
	}
	
	public ModerationBan getUserBan(int userID)
	{
		ModerationBan ban = this.newModerationBan();
		ban.userID = userID;
		
		// Load and validate the ban
		if (Environment.getDatabase().load(ban))
		{
			if (this.banIsValid(ban))
			{
				return ban;
			}
		}
		
		// Not found / expired
		return null;
	}
	
	private boolean banIsValid(ModerationBan ban)
	{
		// Ban expired?
		if (new Date(TimeHelper.getTime()).after(ban.expiresAt))
		{
			// Try to delete ban from the system
			if (this.deleteBan(ban))
			{
				Log.info("ModerationCenter: ban #" + ban.ID + " expired. [user ID: " + ban.userID + ", IP address: " + ban.ip + "]");
				
				// Ban expired aka not valid anymore!
				return false;
			}
		}
		
		// Ban is still valid!
		return true;
	}
	
	public boolean deleteBan(ModerationBan ban)
	{
		if (ban != null)
		{
			return Environment.getDatabase().delete(ban);
		}
		else
		{
			return false;
		}
	}
	
	public ModerationBan setUserBan(int userID, boolean banIP, int hours, String reason, int issuerID)
	{
		// Get users last access entry
		UserAccessEntry lastAccessEntry = HabboHotel.getAccessControl().getLatestAccessEntry(userID);
		if (lastAccessEntry != null)
		{
			// Delete old user ban (if exists)
			ModerationBan ban = this.getUserBan(userID);
			if (ban != null)
			{
				this.deleteBan(ban);
			}
			
			// Delete old ip ban (if exists)
			if (banIP)
			{
				ban = this.getIpBan(lastAccessEntry.ip);
				if (ban != null)
				{
					this.deleteBan(ban);
				}
			}
			
			// Create the new ban
			ban = this.newModerationBan();
			ban.userID = userID;
			ban.ip = (banIP) ? lastAccessEntry.ip : null;
			ban.appliedBy = issuerID;
			ban.reason = reason;
			
			// Work out expiration etc
			Calendar calendar = Calendar.getInstance();
			ban.appliedAt = calendar.getTime();
			if (hours > 0)
			{
				calendar.add(Calendar.HOUR, hours);
			}
			else
			{
				calendar.add(Calendar.YEAR, 10);
			}
			ban.expiresAt = calendar.getTime();
			
			// Insert in in the Database
			if (Environment.getDatabase().insert(ban))
			{
				if (ban.ip == null)
				{
					// Disconnect and notify the user
					CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(ban.userID);
					if (client != null)
					{
						client.sendBan(ban);
						client.stop("user is banned");
					}
				}
				else
				{
					// Disconnect and notify the users on this ip
					Vector<CommunicationHandler> clients = HabboHotel.getGameClients().getClientsWithIpAddress(ban.ip);
					for (CommunicationHandler client : clients)
					{
						if (client != null)
						{
							client.sendBan(ban);
							client.stop("user IP is banned");
						}
					}
				}
				return ban;
			}
		}
		
		// Ban failed!
		return null;
	}
	
	public CallForHelp createCallForHelp()
	{
		return new CallForHelp(++m_callCounter);
	}
	
	public void submitCallForHelp(CallForHelp call)
	{
		// Add to pending calls!
		synchronized (m_pendingCalls)
		{
			m_pendingCalls.add(call);
		}
		
		// Broadcast to helpers
		ServerMessage notify = new ServerMessage("CRYFORHELP");
		notify.appendObject(call);
		this.broadcastToHelpers(notify);
	}
	
	public boolean pickCallForHelp(int callID, String picker)
	{
		CallForHelp call = this.getPendingCall(callID);
		if (call != null)
		{
			// Answered
			synchronized (m_pendingCalls)
			{
				m_pendingCalls.remove(call);
			}
			
			// Notify moderators call is picked
			ServerMessage notify = new ServerMessage("PICKED_CRY");
			notify.appendNewArgument(picker);
			notify.appendNewArgument(ModerationCenter.craftChatlogUrl(callID));
			this.broadcastToHelpers(notify);
			
			// Picked
			return true;
		}
		
		// Already picked / does not exist
		return false;
	}
	
	public void broadcastToHelpers(ServerMessage msg)
	{
		byte minimumRole = HabboHotel.getAccessControl().getMinimumRoleForUserRight("can_answer_cfh");
		Vector<CommunicationHandler> receivers = HabboHotel.getGameClients().getClientsWithUserRole(minimumRole);
		for (CommunicationHandler comm : receivers)
		{
			comm.sendMessage(msg);
		}
	}
	
	public CallForHelp getPendingCall(int callID)
	{
		synchronized (m_pendingCalls)
		{
			for (CallForHelp call : m_pendingCalls)
			{
				if (call.ID == callID)
				{
					return call;
				}
			}
		}
		
		return null;
	}
	
	public int clearPendingCalls()
	{
		synchronized (m_pendingCalls)
		{
			int amount = m_pendingCalls.size();
			m_pendingCalls.clear();
			return amount;
		}
	}
	
	public Vector<CallForHelp> getPendingCalls()
	{
		return m_pendingCalls;
	}
	
	public static String craftChatlogUrl(int callID)
	{
		return "/chatlog.php?id=" + callID;
	}
	
	public static int parseCallID(String chatlogUrl)
	{
		try
		{
			return Integer.parseInt(chatlogUrl.substring("/chatlog.php?id=".length()));
		}
		catch (Exception ex)
		{
			return -1;
		}
	}
	
	/**
	 * Creates a new instance of the ModerationBan DataObject implementation class and returns it.
	 */
	public ModerationBan newModerationBan()
	{
		try
		{
			return (ModerationBan)m_moderationBanClass.newInstance();
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
}
