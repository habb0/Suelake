package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;
import com.suelake.habbo.spaces.pathfinding.RotationCalculator;

public class LOOKTO implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Gather coordinates of tile to make avatar 'look to'
		short tileX = Short.parseShort(msg.nextArgument());
		short tileY = Short.parseShort(msg.nextArgument());
		
		// Get user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// Can not rotate while moving
		if(usr.goalX == -1)
		{
			// Cannot click self
			if(!(usr.X == tileX && usr.Y == tileY))
			{
				// Cannot rotate while sitting or laying
				if(!usr.hasStatus("sit") && !usr.hasStatus("lay"))
				{
					byte rotation = RotationCalculator.calculateHumanDirection(usr.X, usr.Y, tileX, tileY);
					if(rotation != usr.headRotation && rotation != usr.bodyRotation)
					{
						usr.headRotation = rotation;
						usr.bodyRotation = rotation;
						usr.ensureUpdate(true);
					}
				}
			}
		}
	}
}
