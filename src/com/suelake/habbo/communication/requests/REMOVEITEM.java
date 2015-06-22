package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class REMOVEITEM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		if(comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatOwner)
		{
			// Pickup item
			int itemID = Integer.parseInt(msg.nextArgument('/'));
			Item item = comm.getSpaceInstance().getInteractor().pickupWallItem(itemID);
			
			// Picked up? Then delete...
			if(item != null)
			{
				HabboHotel.getItemAdmin().deleteItem(item);
			}
		}
	}
}
