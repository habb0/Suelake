package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class USEITEM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String[] data = msg.getBody().split("\t", 2);
		int length = Integer.parseInt(data[1]);
		if(length < 1000 || length > 5000)
		{
			// No permanent hand to mouth etc
			length = 1000;
		}
		
		// Calculate in seconds
		length /= 1000;
		
		// Add status
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		usr.addStatus("usei", data[0], length, null, 0, 0);
	}
}
