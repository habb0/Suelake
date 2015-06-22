package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_ACCEPTBUDDY implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name of the user to accept
		String buddyName = msg.nextArgument();
		
		// Handle it
		HabboHotel.getMessengerService().acceptBuddyRequest(comm.getUserObject(), buddyName);
	}
}
