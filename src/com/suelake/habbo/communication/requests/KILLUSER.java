package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class KILLUSER implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get this user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// In order to kick users, you require flat controller
		if (usr.isFlatController)
		{
			// Get target user
			SpaceUser usr2 = comm.getSpaceInstance().getUserByName(msg.nextArgument());
			
			// Verify that target user exists
			if (usr2 != null)
			{
				// If it's a flat owner, then you need atleast the same user role!
				if (!usr2.isFlatOwner || usr.getUserObject().role >= usr2.getUserObject().role)
				{
					usr2.getCommunicator().kickFromSpace(null);
				}
			}
		}
	}
}
