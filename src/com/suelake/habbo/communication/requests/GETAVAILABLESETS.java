package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class GETAVAILABLESETS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// This is used for Figure and such. Basically the "sets" that are available to the client.
		//String clientSets = HabboHotel.getPropBox().get("client.sets");
		String clientSets = new String();
		
		// Logged in?
		if (comm.getUserObject() == null)
		{
			clientSets = HabboHotel.getPropBox().get("client.sets");
		}
		else
		{
			// Is this User subscribed to HC or not)
			if (comm.getUserObject().isHC() == false)
			{
				clientSets = HabboHotel.getPropBox().get("client.sets");
			}
			else
			{
				clientSets = HabboHotel.getPropBox().get("client.hc.sets");
			}
		}
		
		// We don't want sets to be nothing...
		if (clientSets != null)
		{
			comm.response.set("AVAILABLESETS");
			comm.response.appendNewArgument(clientSets);
			comm.sendResponse();
		}
		else
		{
			comm.systemMsg("The server was unable to locate the appropriate figuresets!\rContact administrator.");
		}
	}
}
