package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceInstanceInteractor;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class DICE_OFF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse ID
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		
		// Get the object
		Item obj = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
		if(obj != null && obj.definition.behaviour.isDice)
		{
			// Is user standing next to the object?
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if(usr != null && SpaceInstanceInteractor.mapTilesTouch(obj.X, obj.Y, usr.X, usr.Y))
			{
				// Notify clients
				ServerMessage notify = new ServerMessage("DICE_VALUE");
				notify.appendArgument(Integer.toString(itemID));
				notify.appendArgument(Integer.toString(itemID * 38));
				comm.getSpaceInstance().broadcast(notify);
				
				// Save object (dataclass: VALUE)
				obj.customData = "0";
				HabboHotel.getItemAdmin().updateItem(obj);
			}
		}
	}
}
