package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class GETFVRF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get the favorite flat lists
		Vector<Space> flats = HabboHotel.getSpaceAdmin().getFavoriteFlatListForUser(comm.getUserObject().ID);
		
		// Build response
		comm.response.set("FAVORITE_FLAT_RESULTS");
		for (Space flat : flats)
		{
			// Hide name?
			if(!SecurityUtil.canSeeFlatOwner(flat, comm.getUserObject()))
			{
				flat.owner = "-";
			}
			comm.response.appendObject(flat);
		}
		
		// Send response
		comm.sendResponse();
	}
}
