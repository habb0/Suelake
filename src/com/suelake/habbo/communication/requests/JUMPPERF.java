package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class JUMPPERF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get user and object on tile
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		Item obj = comm.getSpaceInstance().getInteractor().getPassiveObjectOnTile(usr.X, usr.Y);
		
		// User is indeed diving?
		if (usr != null && obj != null && obj.definition.sprite.equals("poolLift"))
		{
			// Parse dive data
			String name = msg.nextArgument((char)13);
			String figure = msg.nextArgument((char)13);
			String poolFigure = msg.nextArgument((char)13);
			String data = msg.nextArgument((char)13);
			
			// Start replay for diving user, show user on camera for other clients
			ServerMessage notify = new ServerMessage("JUMPDATA");
			notify.appendNewArgument(comm.getUserObject().name);
			notify.appendNewArgument(data);
			comm.getSpaceInstance().broadcast(notify);
		}
	}
}
