package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;


public class SRCHF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// We do not want searching with additional wildcards
		String criteria = msg.nextArgument('/').replace("%", "");
		
		Vector<Space> flats = HabboHotel.getSpaceAdmin().findFlatsMatchingCriteria(criteria);
		if (flats.size() == 0)
		{
			comm.response.set("NOFLATS");
		}
		else
		{
			comm.response.set("FLAT_RESULTS");
			for (Space flat : flats)
			{
				// Can see owner?
				if(!SecurityUtil.canSeeFlatOwner(flat, comm.getUserObject()))
				{
					// Skip the flat if user searched on the (hidden) ownername
					if(flat.owner.toLowerCase().equals(criteria))
					{
						continue;
					}
					else
					{
						flat.owner = "-";
					}
				}
				comm.response.appendObject(flat);
			}
		}
		
		comm.sendResponse();
	}
}
