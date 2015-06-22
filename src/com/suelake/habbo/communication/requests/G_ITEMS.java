package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class G_ITEMS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.response.set("ITEMS");
		for(Item item : comm.getSpaceInstance().getInteractor().getWallItems())
		{
			comm.response.appendObject(item);
		}
		comm.sendResponse();
	}
}
