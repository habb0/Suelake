package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_SENDUPDATE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Wacky client sends MESSENGER_SENDUPDATE even when user is NOT logged in!
		if(comm.getUserObject() == null) return;
		
		// Update last activity to NOW
		comm.getUserObject().updateLastActivity();
		
		// Re-send buddy list
		comm.getMessenger().sendBuddyList();
	}
}
