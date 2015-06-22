package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class ADDSTRIPITEM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Is this user a flat owner?
		if(comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatOwner)
		{
			// Parse data
			String event = msg.nextArgument();
			String type = msg.nextArgument();
			int itemID = Integer.parseInt(msg.nextArgument());
			
			// Attempt to pickup
			Item item = null;
			if(type.equals("stuff"))
			{
				item  = comm.getSpaceInstance().getInteractor().pickupActiveObject(itemID);
			}
			else if(type.equals("item"))
			{
				item = comm.getSpaceInstance().getInteractor().pickupWallItem(itemID);
			}
			
			// Pickup OK?
			if(item != null)
			{
				// Item is this user's inventory now!
				item.ownerID = comm.getUserObject().ID;
				comm.getItemInventory().addItem(item);
				comm.getItemInventory().sendStrip("last");
				
				// Update item in Database
				HabboHotel.getItemAdmin().updateItem(item);
			}
		}
	}
}
