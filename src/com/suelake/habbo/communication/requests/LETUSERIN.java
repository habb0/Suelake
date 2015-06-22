package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class LETUSERIN implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name of user to let in (this user was ringing the bell)
		String name = msg.nextArgument();
		
		// Attempt to let the user in
		comm.getSpaceInstance().answerDoorbell(comm.clientID, name);
	}
}
