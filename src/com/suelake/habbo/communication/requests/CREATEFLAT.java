package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class CREATEFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// CREATEFLAT /first floor/My room/model_b/open/1"
		
		// Create new Space data object for database implementation
		Space space = HabboHotel.getSpaceAdmin().newSpace();
		if (space != null)
		{
			// Fo real?
			String floor = msg.nextArgument('/');
			if (floor.equals("first floor"))
			{
				// Gather data
				space.name = SecurityUtil.filterInput(msg.nextArgument('/'));
				space.model = msg.nextArgument('/');
				space.accessType = msg.nextArgument('/');
				space.showOwner = (msg.nextArgument('/').equals("1"));
				
				// Create flat
				space.ownerID = comm.getUserObject().ID;
				if (HabboHotel.getSpaceAdmin().createFlat(space))
				{
					comm.response.set("FLATCREATED");
					comm.response.appendArgument(Integer.toString(space.ID));
					comm.response.appendArgument(space.name);
					comm.sendResponse();
				}
				else
				{
					comm.systemError("Could not create your flat for whatever reason!\rTry again or contact administrator.");
				}
			}
		}
	}
}
