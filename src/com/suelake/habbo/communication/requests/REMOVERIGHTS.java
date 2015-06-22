package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class REMOVERIGHTS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get this user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// In order to remove rights, you require flat owner
		if(usr.isFlatOwner)
		{
			// Get target user
			SpaceUser usr2 = comm.getSpaceInstance().getUserByName(msg.nextArgument());
			
			// Verify that target user exists
			if(usr != null)
			{
				// Target is not owner? (Cannot remove owner rights)
				if(!usr2.isFlatOwner)
				{
					// Attempt to remove
					if(comm.getSpaceInstance().removeFlatController(usr2.getUserObject().ID))
					{
						// Mark his SpaceUser instance as not-flat controller and resend user status
						usr2.isFlatController = false;
						usr2.refreshFlatPrivileges();
						usr2.getCommunicator().sendMessage(new ServerMessage("YOUARENOTCONTROLLER"));
					}
				}
			}
		}
	}
}
