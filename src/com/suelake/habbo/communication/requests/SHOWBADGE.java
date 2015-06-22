package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class SHOWBADGE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String badge = comm.getUserObject().badge;
		if(badge != null)
		{
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			usr.addStatus("mod", badge, 0, null, 0, 0);
		}
	}
}
