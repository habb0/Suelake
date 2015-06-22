package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class TRADE_OPEN implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name of the 'target user'
		String name = msg.nextArgument((char)9);
		
		// Get self and target user
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		SpaceUser usr2 = comm.getSpaceInstance().getUserByName(name);
		
		// Both users exist?
		if(usr != null && usr2 != null)
		{
			// Not trading yet?
			if(!comm.getItemTrader().busy())
			{
				// Partner not trading yet?
				if(!usr2.getCommunicator().getItemTrader().busy())
				{
					// Add trading statuses
					usr.addStatus("trd", null, 0, null, 0, 0);
					usr2.addStatus("trd", null, 0, null, 0, 0);
					
					// Link ItemTraderHandlers
					comm.getItemTrader().open(usr2.getCommunicator().clientID);
					usr2.getCommunicator().getItemTrader().open(comm.clientID);
					
					// Refresh tradeboxes for both clients
					comm.getItemTrader().refreshClients();
				}
			}
		}
	}
}
