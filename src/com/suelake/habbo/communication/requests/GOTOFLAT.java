package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class GOTOFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get flat ID
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		
		// Is client trying to enter the flat it authenticated for? If so, forward to space
		if(flatID == comm.authenticatedFlat)
		{
			comm.goToSpace(flatID);
		}
	}
}
