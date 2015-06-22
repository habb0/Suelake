package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;


public class GETALLUNITS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.response.set("UNITS");
		
		Vector<Space> spaces = HabboHotel.getSpaceAdmin().findPublicSpaces();
		for (Space space : spaces)
		{
			comm.response.appendObject(space);
		}
		
		comm.sendResponse();
	}
}
