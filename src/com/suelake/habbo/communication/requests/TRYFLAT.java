package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.spaces.instances.SpaceInstance;

public class TRYFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get flat data & entry password (optional)
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		String password = msg.nextArgument('/');
		Space flat = HabboHotel.getSpaceAdmin().getSpaceInfo(flatID);
		
		// Valid Space?
		if (flat != null && flat.isUserFlat())
		{
			// Is this user entering the flat through a teleporter?
			if (comm.authenticatedTeleporter > 0)
			{
				// Is this user entering a different flat via authenticated teleporter? (elite hax!)
				if (flatID != comm.authenticatedFlat)
				{
					return;
				}
			}
			else
			{
				// Is this user owner of the flat to enter? If so, it can override checks
				if (comm.getUserObject().ID != flat.ownerID)
				{
					// Override checks if user has right to
					if (!comm.getUserObject().hasRight("can_enter_any_room"))
					{
						// Is this flat protected by a password?
						if (flat.accessType.equals("password"))
						{
							// Is the correct password given?
							if (!password.equals(flat.password))
							{
								comm.systemError("Incorrect flat password");
								return;
							}
						}
						else if (flat.accessType.equals("closed"))
						{
							// Try to get running instance of room (if none exist, none is created!)
							SpaceInstance instance = HabboHotel.getSpaceDirectory().getInstance(flatID, false);
							if (instance != null)
							{
								// Somebody that can answer doorbell in this room?
								if (instance.ringDoorbell(comm.getUserObject().name))
								{
									instance.registerClient(comm.clientID);
									comm.waitingForFlatDoorbell = true;
								}
							}
							
							// Wait for eventual doorbell answer
							return;
						}
					}
				}
				
				// OK!
				comm.authenticatedFlat = flatID;
			}
			
			// Fine
			comm.response.set("FLAT_LETIN");
			comm.sendResponse();
		}
	}
}
