package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.util.SecurityUtil;

public class SETITEMDATA implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		if (comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatController)
		{
			int itemID = Integer.parseInt(msg.nextArgument('/'));
			Item item = comm.getSpaceInstance().getInteractor().getWallItem(itemID);
			
			// Item found?
			if (item != null)
			{
				// Item a post.it?
				if (item.definition.behaviour.isPostIt)
				{
					// Parse data
					String[] data = msg.getRemainingBody().split(" ", 2);
					if (!SecurityUtil.postItColorValid(data[0]))
					{
						comm.systemMsg("Sorry mate, but this isn't a valid post.it color.\rAre you using a jewish client or what?\rOh well, go ahead, you have a scripted post.it now!");
					}
					//else // Remove this to allow scripting
					{
						item.customData = data[0]; // Color
						item.itemData = data[1]; // Message
						if (item.customData.length() > 684)
						{
							item.customData = item.customData.substring(0, 684);
						}
						
						// Notify clients
						ServerMessage notify = new ServerMessage("ADDITEM"); // UPDATEITEM does not work (client not finished!)
						notify.appendObject(item);
						comm.getSpaceInstance().broadcast(notify);
						
						// Update item
						HabboHotel.getItemAdmin().updateItem(item);
					}
				}
			}
		}
	}
}
