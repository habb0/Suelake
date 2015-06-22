package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class DEL_FAVORITE_ROOM implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse the ID
		int spaceID = Integer.parseInt(msg.nextArgument());
		
		// Delete from list
		HabboHotel.getSpaceAdmin().modifyFavoriteFlatListForUser(comm.getUserObject().ID, spaceID, false);
	}
}
