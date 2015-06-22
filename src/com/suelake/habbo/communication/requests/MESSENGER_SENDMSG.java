package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_SENDMSG implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Sort the data
		String[] data = msg.getBody().split("\r", 2);
		String[] szReceiverIDs = data[0].split(" ");
		
		// Parse the receiver IDs
		int[] receiverIDs = new int[szReceiverIDs.length];
		for(int i = 0; i < receiverIDs.length; i++)
		{
			try
			{
				int receiverID = Integer.parseInt(szReceiverIDs[i]);
				receiverIDs[i] = receiverID;
			}
			catch(NumberFormatException ex)
			{
				// Bad monkey - caught in the act
				comm.stop("scripting at MESSENGER_SENDMSG");
				return;
			}
		}
		
		// Handle the storage and delivery!
		HabboHotel.getMessengerService().sendMessage(comm.getUserObject().ID, comm.getUserObject().figure, receiverIDs, data[1]);
	}
}
