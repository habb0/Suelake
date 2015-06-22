package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;

public class ADD_FAVORITE_ROOM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse the ID and get the flat
		int spaceID = Integer.parseInt(msg.nextArgument());
		Space space = HabboHotel.getSpaceAdmin().getSpaceInfo(spaceID);
		
		// Exists and a user flat?
		if(space != null && space.isUserFlat())
		{
			HabboHotel.getSpaceAdmin().modifyFavoriteFlatListForUser(comm.getUserObject().ID, spaceID, true);
		}
	}
}
