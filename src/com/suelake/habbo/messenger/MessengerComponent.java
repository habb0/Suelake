package com.suelake.habbo.messenger;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;

public class MessengerComponent
{
	private CommunicationHandler m_client;
	
	public MessengerComponent(CommunicationHandler comm)
	{
		m_client = comm;
	}
	
	public void sendBuddyList()
	{
		// Load buddies from database
		Vector<MessengerBuddy> buddies = HabboHotel.getMessengerService().getBuddies(m_client.getUserObject().ID);
		
		// Build the buddy list message
		ServerMessage msg = new ServerMessage("BUDDYLIST");
		for(MessengerBuddy buddy : buddies)
		{
			msg.appendObject(buddy);
		}
		m_client.sendMessage(msg);
	}
	
	public void sendUnreadMessages()
	{
		ServerMessage msg = new ServerMessage();
		for(MessengerMessage unreadMsg : HabboHotel.getMessengerService().getUnreadMessages(m_client.getUserObject().ID))
		{
			msg.set("MESSENGER_MSG");
			msg.appendObject(unreadMsg);
			
			m_client.sendMessage(msg);
		}
	}
	
	public void sendBuddyRequests()
	{
		ServerMessage msg = new ServerMessage();
		for(String name : HabboHotel.getMessengerService().getBuddyRequests(m_client.getUserObject().ID))
		{
			msg.set("BUDDYADDREQUESTS");
			msg.append("\r");
			msg.appendPartArgument(name);
			
			m_client.sendMessage(msg);
		}
	}
	
	public CommunicationHandler getBuddyClient(int userID)
	{
		return HabboHotel.getGameClients().getClientOfUser(userID);
	}
	
	public CommunicationHandler getClient()
	{
		return m_client;
	}
}
