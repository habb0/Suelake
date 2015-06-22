package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class SBUSYF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// What part do we need?
		String[] data = msg.nextArgument('/').split(",", 2);
		int start = Integer.parseInt(data[0]);
		int stop = Integer.parseInt(data[1]);
		
		// Max amount of flats in one blow: 100
		while ((stop - start) > 100)
		{
			stop--;
		}
		
		comm.response.set("BUSY_FLAT_RESULTS");
		
		// Get the flats!
		Vector<Space> flats = HabboHotel.getSpaceAdmin().findBusyFlats(start, stop);
		for (Space flat : flats)
		{
			// Hide name?
			if(!SecurityUtil.canSeeFlatOwner(flat, comm.getUserObject()))
			{
				flat.owner = "-";
			}
			comm.response.appendObject(flat);
		}
		
		comm.sendResponse();
	}
}
