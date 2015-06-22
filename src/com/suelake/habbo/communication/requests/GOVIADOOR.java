package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.instances.SpaceInstance;

public class GOVIADOOR implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Parse data
		int spaceID = Integer.parseInt(msg.nextArgument('/'));
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		
		// Authenticated for this teleporter?
		if (comm.authenticatedFlat == spaceID && comm.authenticatedTeleporter == itemID)
		{
			// Get instance of Space
			SpaceInstance instance = HabboHotel.getSpaceDirectory().getInstance(spaceID, true);
			if (instance != null)
			{
				// Register client with space instance
				if (instance.registerClient(comm.clientID))
				{
					// OK!
					comm.response.set("OPC_OK");
					comm.sendResponse();
				}
			}
		}
	}
}
