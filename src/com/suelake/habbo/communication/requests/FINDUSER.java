package com.suelake.habbo.communication.requests;

import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.users.User;

public class FINDUSER implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get name from message
		String[] args = msg.getBody().split("\t", 2);
		String name = args[0];
		String system = args[1]; // REGNAME, MESSENGER etc
		
		// Get requested user from register
		User usr = HabboHotel.getUserRegister().getUserInfo(name, false);
		
		// User exists?
		if (usr == null)
		{
			comm.response.set("NOSUCHUSER");
			comm.response.appendArgument(system);
		}
		else
		{
			comm.response.set("MEMBERINFO");
			comm.response.appendArgument(system);
			
			comm.response.appendNewArgument(usr.name);
			comm.response.appendNewArgument(usr.messengerMotto);
			
			CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(usr.ID);
			if(client == null)
			{
				comm.response.appendNewArgument(""); // shows 'offline'
				comm.response.appendNewArgument(TimeHelper.formatDateTime(usr.lastActivity));
			}
			else
			{
				if(client.getSpaceInstance() == null)
				{
					comm.response.appendNewArgument("On Hotel View");
				}
				else if(client.getSpaceInstance().getInfo().isUserFlat())
				{
					comm.response.appendNewArgument("In a user flat");
				}
				else
				{
					comm.response.appendNewArgument(client.getSpaceInstance().getInfo().name);
				}
				comm.response.appendNewArgument(TimeHelper.formatDateTime());
			}
			comm.response.appendNewArgument(usr.figure);
		}
		
		// Send the response
		comm.sendResponse();
	}
}
