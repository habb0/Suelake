package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

public class MOVESTUFF implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Is this user a flat controller?
		if (comm.getSpaceInstance().getUserByClientID(comm.clientID).isFlatController)
		{
			// Parse the data
			int itemID = Integer.parseInt(msg.nextArgument());
			short newX = Short.parseShort(msg.nextArgument());
			short newY = Short.parseShort(msg.nextArgument());
			byte newRotation = Byte.parseByte(msg.nextArgument());
			
			// Attempt move
			if(!comm.getSpaceInstance().getInteractor().moveActiveObject(itemID, null, newX, newY, newRotation))
			{
				// Moving failed? Well atleast refresh for client then
				Item obj = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
				if(obj != null)
				{
					comm.response.set("ACTIVEOBJECT_UPDATE");
					comm.response.appendObject(obj);
					comm.sendResponse();
				}
			}
		}
	}
}
