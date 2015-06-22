package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGERINIT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Send messenger motto
		comm.response.set("MYPERSISTENTMSG");
		comm.response.appendArgument(comm.getUserObject().messengerMotto);
		comm.sendResponse();
		
		// Send the buddy list
		comm.getMessenger().sendBuddyList();
		
		// Send the pending buddy requests
		comm.getMessenger().sendBuddyRequests();
		
		// Send the unread messages
		comm.getMessenger().sendUnreadMessages();
		
		// Send SMS account
		comm.response.set("MESSENGERSMSACCOUNT");
		comm.response.appendArgument("noaccount");
		//comm.response.appendArgument(comm.getUserObject().phoneNumber);
		comm.sendResponse();
		
		// Notify that messenger is ready
		comm.response.set("MESSENGERREADY");
		comm.sendResponse();
		
		// Apply handlers
		comm.getRequestHandlers().registerMessengerHandlers(true);
	}
}
