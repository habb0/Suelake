package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceInstance;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class GETUNITUSERS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get space instance
		String unitName = msg.nextArgument('/');
		SpaceInstance instance = HabboHotel.getSpaceDirectory().getUnitByName(unitName);
		
		comm.response.set("UNITMEMBERS");
		if (instance != null)
		{
			for (SpaceUser usr : instance.getUsers())
			{
				comm.response.appendNewArgument(usr.getUserObject().name);
			}
		}
		comm.sendResponse();
	}
}
