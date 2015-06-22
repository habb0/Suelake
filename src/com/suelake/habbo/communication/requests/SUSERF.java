package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;


public class SUSERF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		Vector<Space> flats = HabboHotel.getSpaceAdmin().findFlatsForUser(comm.getUserObject().ID);
		if (flats.size() == 0)
		{
			comm.response.set("NOFLATSFORUSER");
		}
		else
		{
			comm.response.set("FLAT_RESULTS");
			for (Space flat : flats)
			{
				comm.response.appendObject(flat);
			}
		}
		
		comm.sendResponse();
	}
}
