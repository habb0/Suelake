package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.moderation.ModerationCenter;

public class PICK_CRYFORHELP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse call ID from chatlog URL
		int callID = ModerationCenter.parseCallID(msg.getBody());
		
		// Valid chatlog URL?
		if(callID != -1)
		{
			// Not picked up already?
			if(!HabboHotel.getModerationCenter().pickCallForHelp(callID, comm.getUserObject().name))
			{
				comm.systemMsg("This call has already been picked up!");
			}
		}
	}
}
