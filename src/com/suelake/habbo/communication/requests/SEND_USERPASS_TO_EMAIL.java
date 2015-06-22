package com.suelake.habbo.communication.requests;


import com.blunk.Log;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class SEND_USERPASS_TO_EMAIL implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String name = msg.nextArgument();
		String email = msg.nextArgument();
		comm.systemMsg("Sorry, but this feature has not been implemented in the server yet.");
		
		Log.debug("SEND_USERPASS_TO_EMAIL for " + name + " (" + email + ")");
	}
}
