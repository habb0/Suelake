package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class TRADE_ADDITEM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Trading?
		if (comm.getItemTrader().busy())
		{
			// Get item to offer
			int itemID = Integer.parseInt(msg.nextArgument((char)9));
			Item item = comm.getItemInventory().getItem(itemID);
			
			// Item in inventory?
			if (item != null)
			{
				// Item tradeable?
				if (true)
				{
					// Add item to this User's offer
					comm.getItemTrader().offerItem(item);
					
					// Refresh clients
					comm.getItemTrader().refreshClients();
				}
				else
				{
					comm.systemMsg("Sorry, but you can't trade this item!");
				}
			}
		}
	}
}
