package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class UPDATEFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get flat data
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		Space flat = HabboHotel.getSpaceAdmin().getSpaceInfo(flatID);
		
		// Flat found and owner of this flat?
		if (flat != null && flat.ownerID == comm.getUserObject().ID)
		{
			// Evaluate new data
			flat.name = SecurityUtil.filterInput(msg.nextArgument('/'));
			flat.accessType = msg.nextArgument('/');
			flat.showOwner = (msg.nextArgument('/').equals("1"));
			
			// Update data
			HabboHotel.getSpaceAdmin().updateSpaceInfo(flat);
		}
	}
}
