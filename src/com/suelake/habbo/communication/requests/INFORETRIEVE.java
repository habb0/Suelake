package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class INFORETRIEVE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Request does contain username and password 8-)
		// They probably do just a logincheck at LOGIN and load the user object here
		
		// Generate the protocol representation of the user object
		comm.response.set("USEROBJECT");
		
		// < Go ahead Mike :) >
		// Mike gettin' down 'n' dirty. ya'dig?!?'
		comm.response.appendObject(comm.getUserObject());
		
		comm.sendResponse();
	}
}
