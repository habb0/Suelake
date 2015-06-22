package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceBot;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class G_STAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Locate user in door of space and broadcast entry to room
		if (!comm.getSpaceInstance().activateUser(comm))
		{
			comm.authenticatedTeleporter = 0;
			comm.kickFromSpace("Invalid user activation for space, your client hasn't been registered with SpaceInstance " + comm.getSpaceInstance().getInfo().ID + ".");
		}
		else
		{
			// Send statuses of all room units in room to this client
			comm.response.set("STATUS");
			for (SpaceBot bot : comm.getSpaceInstance().getBots())
			{
				comm.response.append(bot.getStatusString());
			}
			for (SpaceUser usr : comm.getSpaceInstance().getUsers())
			{
				comm.response.append(usr.getStatusString());
			}
			comm.sendResponse();
		}
	}
}
