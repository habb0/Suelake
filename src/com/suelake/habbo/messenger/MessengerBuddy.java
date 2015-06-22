package com.suelake.habbo.messenger;

import java.sql.Date;

import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * MessengerBuddy represents a user in another users buddy list.
 * @author Nillus
 *
 */
public class MessengerBuddy implements SerializableObject
{
	public int ID;
	public String name;
	public String figure;
	public char sex;
	public String messengerMotto;
	public Date lastActivity;
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendNewArgument(Integer.toString(this.ID));
		msg.appendTabArgument(this.name);
		msg.appendTabArgument(this.messengerMotto);
		
		// Determine location
		String location = "";
		CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(this.ID);
		if(client != null)
		{
			if(client.getSpaceInstance() == null)
			{
				location = "On Hotel View";
			}
			else
			{
				if(client.getSpaceInstance().getInfo().isUserFlat())
				{
					location = "In a user flat";
				}
				else
				{
					location = client.getSpaceInstance().getInfo().name;
				}
			}
		}
		msg.appendNewArgument(location);
		if(client == null)
		{
			msg.appendTabArgument(TimeHelper.formatDateTime(this.lastActivity));
		}
		else
		{
			// Current date & time (user is online)
			msg.appendTabArgument(TimeHelper.formatDateTime());
		}
	}
}
