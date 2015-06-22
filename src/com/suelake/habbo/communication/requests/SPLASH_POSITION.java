package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceTile;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class SPLASH_POSITION implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get user and tile object
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		Item obj = comm.getSpaceInstance().getInteractor().getPassiveObjectOnTile(usr.X, usr.Y);
		
		// User is indeed diving?
		if (usr != null && obj != null && obj.definition.sprite.equals("poolLift"))
		{
			// Determine 'landing position' of jump and position of pool exit
			SpaceTile position = SpaceTile.parse(msg.getBody());
			SpaceTile exit = SpaceTile.parse(obj.customData);
			
			// Display splash for clients
			ServerMessage notify = new ServerMessage("SHOWPROGRAM");
			notify.appendArgument("BIGSPLASH");
			notify.appendArgument("POSITION");
			notify.appendArgument(position.toString());
			comm.getSpaceInstance().broadcast(notify);
			
			// Locate user in pool on landing position
			usr.addStatus("swim", null, 0, null, 0, 0);
			comm.getSpaceInstance().getInteractor().warpUser(usr, position.X, position.Y, true);
			
			// Start moving to pool exit
			comm.getSpaceInstance().getInteractor().startUserMovement(usr, exit.X, exit.Y, false);
			
			// Door is open again!
			comm.getSpaceInstance().showProgram(obj.itemData, "open");
		}
	}
}
