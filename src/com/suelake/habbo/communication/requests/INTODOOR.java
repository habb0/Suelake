package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class INTODOOR implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get teleporter
		int itemID = Integer.parseInt(msg.getBody());
		Item obj = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
		
		// Is this a teleporter?
		if (obj != null && obj.definition.behaviour.isTeleporter)
		{
			// Get user by client ID
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if (usr != null)
			{
				// Can enter teleporter from this position?
				if (((obj.rotation == 0 || obj.rotation == 2) && ((usr.X == obj.X + 1) && (usr.Y == obj.Y))) || (obj.rotation == 4 && ((usr.X == obj.X) && (usr.Y == obj.Y + 1))))
				{
					// Step into the teleporter
					comm.getSpaceInstance().getInteractor().startUserMovement(usr, obj.X, obj.Y, true);
				}
			}
		}
	}
}
