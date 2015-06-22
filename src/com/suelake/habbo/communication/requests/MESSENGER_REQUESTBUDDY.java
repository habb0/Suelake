package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_REQUESTBUDDY implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name of the user to request
		String payload = msg.getRemainingBody();
		String targetName = payload.substring(0, payload.length() - 2);
		
		// Create request
		HabboHotel.getMessengerService().createBuddyRequest(comm.getUserObject(), targetName);
	}
}
