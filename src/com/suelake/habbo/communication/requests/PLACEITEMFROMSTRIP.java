package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.items.ItemDefinition;

public class PLACEITEMFROMSTRIP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int itemID = Integer.parseInt(msg.nextArgument());
		String position = msg.getRemainingBody();
		
		Item item = comm.getItemInventory().getItem(itemID);
		if (item != null)
		{
			if (item.definition.behaviour.onWall)
			{
				if (comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatController)
				{
					// Post.it?
					if(item.definition.behaviour.isPostIt)
					{
						// Determine new pad size
						int padSize = Integer.parseInt(item.customData) - 1;
						if(padSize > 0)
						{
							item.customData = Integer.toString(padSize);
							HabboHotel.getItemAdmin().updateItem(item);
						}
						else
						{
							comm.getItemInventory().removeItem(item.ID);
							HabboHotel.getItemAdmin().deleteItem(item);
						}
						
						// Create post.it from pad
						ItemDefinition def = item.definition;
						item = HabboHotel.getItemAdmin().newItem();
						item.definition = def;
						item.customData = "FFFF33";
						item.itemData = "";
						HabboHotel.getItemAdmin().storeItem(item);
					}
					
					// Attempt to place item
					if (comm.getSpaceInstance().getInteractor().placeWallItem(item, position))
					{
						// Post.its never 'drop' after successful placement
						if(!item.definition.behaviour.isPostIt)
						{
							comm.getItemInventory().removeItem(itemID);
						}
					}
				}
			}
		}
	}
}
