package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.games.WobbleSquabbleHandler;
import com.suelake.habbo.games.WobbleSquabblePlayer;

public class PTM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get handler
		WobbleSquabbleHandler handler = comm.getSpaceInstance().getWobbleSquabbleHandler();
		
		// Game running?
		if (handler != null && handler.gameRunning())
		{
			// Get player
			WobbleSquabblePlayer player = handler.getPlayerByClientID(comm.clientID);
			
			// Player found?
			if (player != null)
			{
				// Determine move
				player.move = msg.getBody().charAt(0);
				if (player.move == 'A' || player.move == 'X' || player.move == 'W')
				{
					player.moveDirection = -1; // Left
				}
				else
				{
					player.moveDirection = 1; // Right
				}
				player.requiresUpdate = true;
			}
		}
	}
}
