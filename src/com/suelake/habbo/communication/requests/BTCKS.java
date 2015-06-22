package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.users.User;

public class BTCKS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Some magic numbers
		final short buyPrice = 2;
		final short ticketAmount = 5;
		
		// Can this user purchase?
		if(comm.getUserObject().credits < buyPrice)
		{
			comm.systemMsg("Sorry, but you do not have enough Credits to purchase this.");
			return;
		}
		
		// For who are the tickets bought?
		String receiverName = msg.nextArgument('/');
		boolean forSelf = (receiverName.equalsIgnoreCase(comm.getUserObject().name));
		
		// Determine receiver
		User rcvr = null;
		if(forSelf)
		{
			rcvr = comm.getUserObject();
		}
		else
		{
			rcvr = HabboHotel.getUserRegister().getUserInfo(receiverName, true);
			if(rcvr == null)
			{
				comm.systemMsg("Sorry, but the user '" + receiverName + "' does not exist!");
				return;
			}
		}
		
		// Refresh buyer
		comm.getUserObject().credits -= buyPrice;
		comm.sendCredits();
		
		// Give tickets to receiver
		rcvr.gameTickets += ticketAmount;
		
		// Update buyer
		HabboHotel.getUserRegister().updateUser(comm.getUserObject());
		
		// Update receiver
		if(forSelf)
		{
			comm.sendGameTickets();
		}
		else
		{
			// Update data of other user
			HabboHotel.getUserRegister().updateUser(rcvr);
			
			// Notify other user if online
			CommunicationHandler client2 = HabboHotel.getGameClients().getClientOfUser(rcvr.ID);
			if(client2 != null)
			{
				client2.sendGameTickets();
				client2.systemMsg(comm.getUserObject().name + " has bought " + ticketAmount + " game tickets for you!");
			}
		}
	}
}
