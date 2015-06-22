package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class ASSIGNRIGHTS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get this user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// In order to assign rights, you require flat owner
		if(usr.isFlatOwner)
		{
			// Get target user
			SpaceUser usr2 = comm.getSpaceInstance().getUserByName(msg.nextArgument());
			
			// Verify that target user exists
			if(usr2 != null)
			{
				// Try to add
				if(comm.getSpaceInstance().addFlatController(usr2.getUserObject().ID))
				{
					// Mark his SpaceUser instance as flat controller and resend user status
					usr2.isFlatController = true;
					usr2.refreshFlatPrivileges();
				}
			}
		}
	}
}
