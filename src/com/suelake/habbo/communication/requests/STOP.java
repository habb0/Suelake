package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class STOP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get the key of the status to remove ('stop')
		String status = msg.nextArgument().toLowerCase();
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// Determine status to remove
		if(status.equals("carryitem"))
		{
			status = "carryd";
		}
		comm.getSpaceInstance().getUserByClientID(comm.clientID).removeStatus(status);
	}
}
