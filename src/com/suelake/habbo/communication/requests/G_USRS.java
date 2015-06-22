package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceBot;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class G_USRS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.response.set("USERS");
		
		// Append bots
		for(SpaceBot bot : comm.getSpaceInstance().getBots())
		{
			comm.response.appendObject(bot);
		}
		
		// Append 'real users'
		for(SpaceUser usr : comm.getSpaceInstance().getUsers())
		{
			if(!usr.isInvisible)
			{
				comm.response.appendObject(usr);
			}
		}
		
		comm.sendResponse();
	}
}
