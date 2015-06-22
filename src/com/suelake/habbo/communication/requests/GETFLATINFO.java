package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.util.SecurityUtil;

public class GETFLATINFO implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get Space object
		int flatID = Integer.parseInt(msg.nextArgument('/'));
		Space flat = HabboHotel.getSpaceAdmin().getSpaceInfo(flatID);
		
		// Valid Space?
		if (flat != null && flat.isUserFlat())
		{
			// Can see owner?
			boolean canSeeOwner = SecurityUtil.canSeeFlatOwner(flat, comm.getUserObject());
			
			// * Nillus touches and strokes matt for this <3
			comm.response.set("FLATINFO");
			comm.response.appendKVArgument("i", Integer.toString(flat.ID));
			comm.response.appendKVArgument("n", flat.name);
			comm.response.appendKVArgument("o", canSeeOwner ? flat.owner : "-");
			comm.response.appendKVArgument("m", flat.accessType);
			comm.response.appendKVArgument("u", "30000"); // Server port
			comm.response.appendKVArgument("w", flat.showOwner ? "1" : "0");
			comm.response.appendKVArgument("t", "127.0.0.1"); // Server IP
			comm.response.appendKVArgument("d", flat.description);
			comm.response.appendKVArgument("a", flat.superUsers ? "1" : "0");
			
			comm.sendResponse();
		}
	}
}
