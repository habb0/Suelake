package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class G_HMAP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.response.set("HEIGHTMAP");
		comm.response.appendArgument(comm.getSpaceInstance().getInteractor().generateHeightMapString());
		comm.sendResponse();
	}
}
