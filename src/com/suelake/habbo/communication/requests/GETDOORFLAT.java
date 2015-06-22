package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.spaces.instances.SpaceUser;

public class GETDOORFLAT implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Get item
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		Item obj = comm.getSpaceInstance().getInteractor().getActiveObject(itemID);
		
		// Item valid?
		if(obj != null && obj.definition.behaviour.isTeleporter)
		{
			// Sleep...
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException ex)
			{
				return;
			}
			
			// Get user
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if(usr != null)
			{
				// User actually in teleporter?
				if(usr.X == obj.X && usr.Y == obj.Y)
				{
					// Get teleporter 2
					Item obj2 = HabboHotel.getItemAdmin().getItem(obj.teleporterID);
					
					// Teleporter 2 is found and in a space?
					if(obj2 != null && obj2.spaceID > 0)
					{
						// Broadcast user going in
						comm.getSpaceInstance().getInteractor().broadcastTeleporterActivity(obj.ID, obj.definition.sprite, comm.getUserObject().name, true);
						
						// Unblock current tile (user goes into the void)
						usr.moveLock = true;
						comm.getSpaceInstance().getInteractor().setUserMapTile(usr.X, usr.Y, false);

						// Serverside teleport
						if(obj2.spaceID == comm.getSpaceInstance().getInfo().ID)
						{
							// Wait for client...
							try
							{
								Thread.sleep(500);
							}
							catch (InterruptedException ex)
							{
								return;
							}
							
							
							// Broadcast user coming out of teleporter 2
							comm.getSpaceInstance().getInteractor().broadcastTeleporterActivity(obj2.ID, obj2.definition.sprite, comm.getUserObject().name, false);
							
							// And warp to teleporter 2 tile
							usr.X = obj2.X;
							usr.Y = obj2.Y;
							usr.Z = obj2.Z;
							usr.headRotation = obj2.rotation;
							usr.bodyRotation = obj2.rotation;
							comm.getSpaceInstance().getInteractor().setUserMapTile(usr.X, usr.Y, true);
							
							// Update state
							usr.moveLock = false;
							usr.ensureUpdate(false);
						}
						else
						{
							// Set IDs
							comm.authenticatedFlat = obj2.spaceID;
							comm.authenticatedTeleporter = obj2.ID;
							
							// Trigger client
							comm.response.set("DOORFLAT");
							comm.response.appendNewArgument(Integer.toString(obj2.ID));
							comm.response.appendNewArgument(Integer.toString(obj2.spaceID));
							comm.sendResponse();
						}
					}
				}
			}
		}
	}
}
