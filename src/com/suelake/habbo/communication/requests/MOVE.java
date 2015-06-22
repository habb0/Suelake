package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class MOVE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Gather destination tile coordinates
		short tileX = Short.parseShort(msg.nextArgument());
		short tileY = Short.parseShort(msg.nextArgument());
		
		// Get SpaceUser
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		
		// Can move?
		if (usr != null && !usr.moveLock)
		{
			// Not trying to restart the route? (as it's pointless...)
			if (tileX != usr.goalX || tileY != usr.goalY)
			{
				// Not clicking own tile?
				if (tileX != usr.X || tileY != usr.Y)
				{
					// Request movement 'trip' in space instance
					// Pathfinder will be invoked in the current thread (so, m_thread in TcpConnection)
					comm.getSpaceInstance().getInteractor().startUserMovement(usr, tileX, tileY, false);
				}
			}
		}
	}
}