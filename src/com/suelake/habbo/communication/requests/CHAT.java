package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceUser;
import com.suelake.habbo.util.ChatCommandParser;
import com.suelake.habbo.util.SecurityUtil;

public class CHAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get text message
		String text = SecurityUtil.filterInput(msg.getBody());
		if (text.trim().length() > 0)
		{
			// Check chat commands
			if (!ChatCommandParser.parseCommand(comm, text))
			{
				// Broadcast talk and animate this user
				SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
				comm.getSpaceInstance().chat(usr, text, false);
			}
		}
	}
}
