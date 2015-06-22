package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class SCR_GINFO implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get the pending amount of HC days
		int pendingDays = comm.getUserObject().getPendingHcDays();
		
		// Has this user a subscription?!
		if(pendingDays == 0)
		{
			comm.response.set("SCR_NOSUB");
		}
		else
		{
			comm.response.set("SCR_SINF");
			comm.response.appendTabArgument("club_habbo");
			comm.response.appendTabArgument("active");
			comm.response.appendTabArgument(Integer.toString(pendingDays));
		}
		
		// Send the response
		comm.sendResponse();
	}
}
