package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class PLACESTUFFFROMSTRIP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int itemID = Integer.parseInt(msg.nextArgument());
		short tileX = Short.parseShort(msg.nextArgument());
		short tileY = Short.parseShort(msg.nextArgument());
		
		Item item = comm.getItemInventory().getItem(itemID);
		if (item != null)
		{
			if (item.definition.behaviour.onFloor)
			{
				if (comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatController)
				{
					if (comm.getSpaceInstance().getInteractor().moveActiveObject(itemID, item, tileX, tileY, (byte)0))
					{
						comm.getItemInventory().removeItem(itemID);
					}
				}
			}
		}
	}
}
