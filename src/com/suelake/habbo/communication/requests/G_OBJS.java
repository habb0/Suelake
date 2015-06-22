package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class G_OBJS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Send the passive objects (objects that will never change state)
		comm.response.set("OBJECTS");
		comm.response.appendArgument("WORLD");
		comm.response.appendArgument("0");
		comm.response.appendArgument(comm.getSpaceInstance().getModel().type);
		for(Item obj : comm.getSpaceInstance().getInteractor().getPassiveObjects())
		{
			if(!obj.definition.behaviour.isInvisible)
			{
				comm.response.appendObject(obj);
			}
		}
		comm.sendResponse();
		
		// Send the active objects (movable floor objects etc)
		comm.response.set("ACTIVE_OBJECTS");
		for(Item obj : comm.getSpaceInstance().getInteractor().getActiveObjects())
		{
			comm.response.appendObject(obj);
		}
		comm.sendResponse();
	}
}
