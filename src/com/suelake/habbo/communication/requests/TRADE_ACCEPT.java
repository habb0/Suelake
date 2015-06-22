package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.ItemTradeHandler;

public class TRADE_ACCEPT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		if(comm.getItemTrader().busy())
		{
			comm.getItemTrader().accept();
			comm.getItemTrader().refreshClients();
			
			// Partner has accepted aswell?
			ItemTradeHandler partner = comm.getItemTrader().getPartner();
			if(partner.accepting())
			{
				// Swap offers
				comm.getItemTrader().castOfferTo(partner);
				partner.castOfferTo(comm.getItemTrader());
				
				// Meep!
				comm.stopTrading();
			}
		}
	}
}
