package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.Space;

public class FLATPROPERTYBYITEM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse data
		String propType = msg.nextArgument('/');
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		
		Item item = comm.getItemInventory().getItem(itemID);
		if(item != null)
		{
			if(item.definition.behaviour.isDecoration)
			{
				if(comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatController)
				{
					// Delete item from inventory
					comm.getItemInventory().removeItem(itemID);
					comm.getItemInventory().sendStrip("update");
					
					// Delete item from Database
					HabboHotel.getItemAdmin().deleteItem(item);
					
					// Apply flat property
					Space flat = comm.getSpaceInstance().getInfo();
					if(item.definition.sprite.equals("wallpaper"))
					{
						flat.wallpaper = Short.parseShort(item.customData);
					}
					else if(item.definition.sprite.equals("floor"))
					{
						flat.floor = Short.parseShort(item.customData);
					}
					else
					{
						comm.systemMsg("Invalid decoration type. Only 'floor' or 'wallpaper' are valid.");
						return;
					}
					
					// Notify clients
					ServerMessage notify = new ServerMessage("FLATPROPERTY");
					notify.appendArgument(item.definition.sprite);
					notify.appendPartArgument(item.customData);
					comm.getSpaceInstance().broadcast(notify);
					
					// Update flat data
					HabboHotel.getSpaceAdmin().updateSpaceInfo(flat);
				}
			}
		}
	}
}
