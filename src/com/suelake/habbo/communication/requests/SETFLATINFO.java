package com.suelake.habbo.communication.requests;

import com.blunk.util.KeyValueStringReader;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class SETFLATINFO implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get Space object
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		Space flat = HabboHotel.getSpaceAdmin().getSpaceInfo(flatID);
		
		// Can edit this flat?
		if (flat != null && flat.ownerID == comm.getUserObject().ID)
		{
			// Evaluate new data
			KeyValueStringReader props = new KeyValueStringReader(msg.getRemainingBody(), "=");
			flat.description = SecurityUtil.filterInput(props.read("description"));
			flat.password = props.read("password");
			flat.superUsers = (props.read("allsuperuser").equals("1"));
			
			// Update data
			HabboHotel.getSpaceAdmin().updateSpaceInfo(flat);
		}
	}
}
