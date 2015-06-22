package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class REMOVESTUFF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		if(comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatOwner)
		{
			int itemID = Integer.parseInt(msg.nextArgument('/'));
			Item obj = comm.getSpaceInstance().getInteractor().pickupActiveObject(itemID);
			if(obj != null)
			{
				HabboHotel.getItemAdmin().deleteItem(obj);
			}
		}
	}
}
