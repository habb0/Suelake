package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class LOGIN implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Gather credentials
		String name = msg.nextArgument();
		String password = msg.nextArgument();
		
		// Now try to login
		comm.login(name, password);
	}
}
