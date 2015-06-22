package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class SIGN implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get ID of sign
		int num = Integer.parseInt(msg.getBody());
		if (num >= 1 && num <= 14)
		{
			// Diving score: 4 ... 10 [1 = 4, 7 = 10]
			if(num >= 1 && num <= 7)
			{
				// TODO: account 'vote on dive'
			}
	
			// Add status
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			usr.removeStatus("dance");
			usr.addStatus("sign", Integer.toString(num), 2, null, 0, 0);
		}
	}
}
