package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class CARRYDRINK implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String item = msg.getBody();
		item = item.replace('/', '?');
			
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		usr.removeStatus("dance");
		usr.removeStatus("drink");
		usr.addStatus("carryd", item, 120, "drink", 12, 1);
	}
}
