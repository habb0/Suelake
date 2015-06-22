package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;

public class DELETEFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		Space flat = HabboHotel.getSpaceAdmin().getSpaceInfo(flatID);
		if(flat != null && flat.ownerID == comm.getUserObject().ID)
		{
			// Delete flat and all content etc
			HabboHotel.getSpaceAdmin().deleteSpace(flat);
		}
	}
}
