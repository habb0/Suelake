package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class DANCE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// Can dance in current context?
		if(!usr.hasStatus("sit") && !usr.hasStatus("lay"))
		{
			// Handle statuses
			usr.removeStatus("carryd");
			usr.removeStatus("drink");
			usr.addStatus("dance", null, 0, null, 0, 0);
		}
	}
}
