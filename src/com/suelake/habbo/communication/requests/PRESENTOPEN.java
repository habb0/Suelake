package com.suelake.habbo.communication.requests;

import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class PRESENTOPEN implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		if (comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatOwner)
		{
			// Get present box item
			int itemID = Integer.parseInt(msg.nextArgument('/'));
			Item box = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
			
			// Present box?
			if (box != null && box.definition.sprite.startsWith("present_gen"))
			{
				// Pickup present box from room
				comm.getSpaceInstance().getInteractor().pickupActiveObject(itemID);
				HabboHotel.getItemAdmin().deleteItem(box);
				
				// Parse article data
				int articleID = 0;
				String articleCustomData = null;
				try
				{
					String[] articleData = box.customData.split(":", 3);
					articleID = Integer.parseInt(articleData[0]);
					if(!articleData[1].equals("*"))
					{
						articleCustomData = articleData[1];
					}
				}
				catch (Exception ex)
				{
					Log.error("PRESENTOPEN: article data parser error for string \"" + box.customData + "\", present deleted.");
				}
				
				// Purchase articles
				Item[] content = HabboHotel.getCatalogue().purchaseArticle(articleID, articleCustomData, comm.getUserObject().ID, comm.getUserObject().ID, false, null);
				if (content == null)
				{
					comm.systemMsg("Sorry, but the contents of this present box are not valid (anymore)!\rIf this happens frequently with the same kind of product, then please contact administrator.");
				}
				else
				{
					// Add content to inventory
					for (Item item : content)
					{
						comm.getItemInventory().addItem(item);
					}
					
					// Refresh inventory
					comm.getItemInventory().sendStrip("last");
					
					// Build response
					comm.response.set("PRESENTOPEN");
					comm.response.appendNewArgument(content[0].definition.sprite);
					if (content[0].definition.sprite.equals("poster"))
					{
						comm.response.appendNewArgument("poster " + content[0].customData);
					}
					else
					{
						comm.response.appendNewArgument(content[0].definition.sprite);
						if (content[0].definition.behaviour.onFloor)
						{
							comm.response.appendNewArgument("");
							comm.response.appendArgument(Byte.toString(content[0].definition.length), '|');
							comm.response.appendArgument(Byte.toString(content[0].definition.width), '|');
							comm.response.appendArgument(content[0].definition.color, '|');
						}
					}
					comm.sendResponse();
				}
			}
		}
	}
}
