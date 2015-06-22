package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_ASSIGNPERSMSG implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get new message
		String newMessage = msg.getBody();
		
		// Update user object
		comm.getUserObject().messengerMotto = newMessage;
		HabboHotel.getUserRegister().updateUser(comm.getUserObject());
	}
}
