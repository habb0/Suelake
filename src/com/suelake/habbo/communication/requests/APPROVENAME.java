package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class APPROVENAME implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name from message
		String name = msg.nextArgument();
		
		// Set response msg type depending on result of the check
		if (HabboHotel.getUserRegister().approveName(name) == true)
		{
			comm.response.set("NAME_APPROVED");
		}
		else
		{
			comm.response.set("NAME_UNACCEPTABLE");
		}
		
		// Send the response
		comm.sendResponse();
	}
}
