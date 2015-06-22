package com.suelake.habbo.messenger;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.users.User;

/**
 * MessengerService provides methods for making the ingame messenger ('Console') function, such as sending messages, looking up buddy lists etc.
 * 
 * @author Nillus
 */
public class MessengerService
{
	private Class<MessengerMessage> m_messengerMessageClass;
	private Class<MessengerBuddyLink> m_messengerBuddyLinkClass;
	
	@SuppressWarnings("unchecked")
	public MessengerService()
	{
		MessengerMessage sample = (MessengerMessage)HabboHotel.getDataObjectFactory().newObject("MessengerMessage");
		if (sample != null)
		{
			Class rawClass = sample.getClass();
			m_messengerMessageClass = rawClass;
		}
		
		MessengerBuddyLink sample2 = (MessengerBuddyLink)HabboHotel.getDataObjectFactory().newObject("MessengerBuddyLink");
		if (sample2 != null)
		{
			Class rawClass = sample2.getClass();
			m_messengerBuddyLinkClass = rawClass;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<MessengerBuddy> getBuddies(int userID)
	{
		MessengerBuddyGrabber grabber = (MessengerBuddyGrabber)HabboHotel.getDataQueryFactory().newQuery("MessengerBuddyGrabber");
		grabber.userID = userID;
		
		return (Vector<MessengerBuddy>)Environment.getDatabase().query(grabber);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<String> getBuddyRequests(int userID)
	{
		MessengerBuddyRequestGrabber grabber = (MessengerBuddyRequestGrabber)HabboHotel.getDataQueryFactory().newQuery("MessengerBuddyRequestGrabber");
		grabber.userID = userID;
		
		return (Vector<String>)Environment.getDatabase().query(grabber);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<MessengerMessage> getUnreadMessages(int userID)
	{
		MessengerMessageGrabber grabber = (MessengerMessageGrabber)HabboHotel.getDataQueryFactory().newQuery("MessengerMessageGrabber");
		grabber.userID = userID;
		
		return (Vector<MessengerMessage>)Environment.getDatabase().query(grabber);
	}
	
	public MessengerBuddyLink getBuddyLink(int id1, int id2)
	{
		MessengerBuddyLink buddy = this.newMessengerBuddyLink();
		buddy.ID1 = id1;
		buddy.ID2 = id2;
		
		// Link exists?
		if (Environment.getDatabase().load(buddy))
		{
			return buddy;
		}
		else
		{
			return null;
		}
	}
	
	public void createBuddyRequest(User user, String targetName)
	{
		// Get UserObject of target
		User target = HabboHotel.getUserRegister().getUserInfo(targetName, true);
		
		// User exists and <= role?
		if (target != null && target.role <= user.role)
		{
			// Not requesting self?
			if (target.ID != user.ID)
			{
				// No pending link yet?
				if (this.getBuddyLink(user.ID, target.ID) == null)
				{
					// Create a new buddy link which is unaccepted
					MessengerBuddyLink link = this.newMessengerBuddyLink();
					link.ID1 = user.ID;
					link.ID2 = target.ID;
					link.isAccepted = false;
					if (Environment.getDatabase().insert(link))
					{
						// Notificate the target (if online)
						CommunicationHandler targetClient = HabboHotel.getGameClients().getClientOfUser(target.ID);
						if (targetClient != null)
						{
							targetClient.getMessenger().sendBuddyRequests();
						}
					}
				}
			}
		}
	}
	
	public void acceptBuddyRequest(User usr, String buddyName)
	{
		User buddy = HabboHotel.getUserRegister().getUserInfo(buddyName, true);
		if (buddy != null)
		{
			// Look for a unaccepted buddy link
			MessengerBuddyLink link = this.getBuddyLink(usr.ID, buddy.ID);
			if (link != null && !link.isAccepted)
			{
				// Flag the buddy link as 'accepted'
				link.isAccepted = true;
				Environment.getDatabase().update(link);
				
				// Update the buddy list on both ends
				CommunicationHandler client;
				if ((client = HabboHotel.getGameClients().getClientOfUser(usr.ID)) != null)
				{
					client.getMessenger().sendBuddyList();
				}
				if ((client = HabboHotel.getGameClients().getClientOfUser(buddy.ID)) != null)
				{
					client.getMessenger().sendBuddyList();
				}
			}
		}
	}
	
	public void declineBuddyRequest(int userID, String buddyName)
	{
		User buddy = HabboHotel.getUserRegister().getUserInfo(buddyName, true);
		if (buddy != null)
		{
			// Look for a unaccepted buddy link
			MessengerBuddyLink link = this.getBuddyLink(userID, buddy.ID);
			if (link != null && !link.isAccepted)
			{
				Environment.getDatabase().delete(link);
			}
		}
	}
	
	public void deleteBuddyLink(int userID, String buddyName)
	{
		User buddy = HabboHotel.getUserRegister().getUserInfo(buddyName, true);
		if (buddy != null)
		{
			// Look for a accepted buddy link
			MessengerBuddyLink link = this.getBuddyLink(userID, buddy.ID);
			if (link != null && link.isAccepted)
			{
				// Delete the link from the database
				Environment.getDatabase().delete(link);
				
				// Update the buddy list on both ends
				CommunicationHandler client;
				if ((client = HabboHotel.getGameClients().getClientOfUser(userID)) != null)
				{
					client.getMessenger().sendBuddyList();
				}
				if ((client = HabboHotel.getGameClients().getClientOfUser(buddy.ID)) != null)
				{
					client.getMessenger().sendBuddyList();
				}
			}
		}
	}
	
	public void sendMessage(int senderID, String senderFigure, int[] receiverIDs, String text)
	{
		// Create message object
		MessengerMessage msg = this.newMessengerMessage();
		msg.senderID = senderID;
		msg.timestamp = TimeHelper.getDateTime();
		msg.text = text.replace((char)13, (char)10);
		msg.senderFigure = senderFigure;
		
		// Deliver to all receivers
		for (int receiverID : receiverIDs)
		{
			// Store message for this receiver
			msg.receiverID = receiverID;
			
			// Stored in database?
			if (Environment.getDatabase().insert(msg))
			{
				// Receiver online? If so, notify that new messenger message has been delivered
				CommunicationHandler receiver = HabboHotel.getGameClients().getClientOfUser(msg.receiverID);
				if (receiver != null)
				{
					ServerMessage notify = new ServerMessage("MESSENGER_MSG");
					notify.appendObject(msg);
					receiver.sendMessage(notify);
				}
			}
		}
	}
	
	public boolean markMessageAsRead(int messageID)
	{
		MessengerMessage msg = this.newMessengerMessage();
		msg.ID = messageID;
		return Environment.getDatabase().delete(msg);
	}
	
	/**
	 * Creates a new instance of the MessengerMessage DataObject implementation class and returns it.
	 */
	public MessengerMessage newMessengerMessage()
	{
		try
		{
			return m_messengerMessageClass.newInstance();
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
	
	/**
	 * Creates a new instance of the MessengerBuddyLink DataObject implementation class and returns it.
	 */
	public MessengerBuddyLink newMessengerBuddyLink()
	{
		try
		{
			return m_messengerBuddyLinkClass.newInstance();
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
