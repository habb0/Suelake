package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class TRADE_UNACCEPT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Trading?
		if(comm.getItemTrader().busy())
		{
			// Unaccept!
			comm.getItemTrader().unaccept();
			
			// Refresh clients!
			comm.getItemTrader().refreshClients();
		}
	}
}
