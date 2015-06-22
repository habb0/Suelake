package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class G_IDATA implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		Item item = comm.getSpaceInstance().getInteractor().getWallItem(itemID);
		if(item != null)
		{
			if(item.definition.behaviour.isPostIt || item.definition.behaviour.isPhoto)
			{
				comm.response.set("IDATA");
				comm.response.appendArgument(Integer.toString(item.ID));
				comm.response.appendTabArgument(item.customData);
				if(item.definition.behaviour.isPhoto)
				{
					comm.response.appendArgument("x ");
				}
				comm.response.appendArgument(item.itemData);
				comm.sendResponse();
			}
		}
	}
}
