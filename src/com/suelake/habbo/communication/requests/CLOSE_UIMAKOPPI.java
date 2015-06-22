package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceTile;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class CLOSE_UIMAKOPPI implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Client sends this when pool figure was updated while in clothing booth
		
		// Get user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		Item obj = comm.getSpaceInstance().getInteractor().getPassiveObjectOnTile(usr.X, usr.Y);
		
		// User in clothing booth?
		if(usr != null && obj != null && obj.definition.sprite.equals("poolBooth"))
		{
			// Re-send user to clients in space
			ServerMessage notify = new ServerMessage("USERS");
			notify.appendObject(usr);
			comm.getSpaceInstance().broadcast(notify);
			
			// Open curtains
			comm.getSpaceInstance().showProgram(obj.itemData, "open");
			
			// Move out of clothing booth
			usr.moveLock = false;
			SpaceTile goal = SpaceTile.parse(obj.customData);
			comm.getSpaceInstance().getInteractor().startUserMovement(usr, goal.X, goal.Y, true);
		}
	}
}
