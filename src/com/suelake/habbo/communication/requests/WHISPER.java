package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.spaces.instances.SpaceUser;
import com.suelake.habbo.util.SecurityUtil;

public class WHISPER implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get receiver & text message
		String receiver = msg.nextArgument();
		String text = SecurityUtil.filterInput(msg.getRemainingBody());
		
		// Get receiver user
		SpaceUser usr2 = comm.getSpaceInstance().getUserByName(receiver);
		if (usr2 != null)
		{
			// Prepare whisper message
			ServerMessage whisper = new ServerMessage("WHISPER");
			whisper.appendArgument(comm.getUserObject().name);
			whisper.appendArgument(text);
			
			// Send to this user and receiver
			comm.sendMessage(whisper);
			usr2.getCommunicator().sendMessage(whisper);
			
			// Eavesdropping
			for(SpaceUser eavesdropper : comm.getSpaceInstance().getUsers())
			{
				// Can this user eavesdrop?
				if(eavesdropper.getUserObject().role != 1 && eavesdropper.getUserObject().hasRight("can_eavesdrop"))
				{
					// Can only eavesdrop users with lower role
					if(eavesdropper.getUserObject().role > comm.getUserObject().role)
					{
						ServerMessage bubble = new ServerMessage("SHOUT");
						bubble.appendArgument(comm.getUserObject().name);
						bubble.appendArgument("[wt: " + usr2.getUserObject().name + "]");
						bubble.appendArgument(text);
						eavesdropper.getCommunicator().sendMessage(bubble);
					}
				}
			}
		}
	}
}
