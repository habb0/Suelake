package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_REMOVEBUDDY implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name of the buddy to remove
		String buddyName = msg.nextArgument();
		
		// Process the removal
		HabboHotel.getMessengerService().deleteBuddyLink(comm.getUserObject().ID, buddyName);
	}
}
