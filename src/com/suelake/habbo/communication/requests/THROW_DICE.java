package com.suelake.habbo.communication.requests;

import com.blunk.Environment;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceInstanceInteractor;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class THROW_DICE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		
		// Get the object
		Item obj = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
		if(obj != null && obj.definition.behaviour.isDice)
		{
			// Is user standing next to the object?
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if(usr != null && SpaceInstanceInteractor.mapTilesTouch(obj.X, obj.Y, usr.X, usr.Y))
			{
				// Generate end value
				int n = Environment.getRandom().nextInt(7);
				if(n == 0)
				{
					n = 1;
				}
				
				// Notify clients dice has stopped at end value
				ServerMessage notify = new ServerMessage("DICE_VALUE");
				notify.appendArgument(Integer.toString(itemID));
				notify.appendArgument(Integer.toString((itemID * 38) + n));
				comm.getSpaceInstance().broadcast(notify);
				
				// Save object (dataclass: VALUE)
				obj.customData = Integer.toString(n);
				HabboHotel.getItemAdmin().updateItem(obj);
			}
		}
	}
}
