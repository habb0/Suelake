package com.suelake.habbo.communication.requests;


import com.blunk.util.KeyValueStringReader;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientCommands;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.users.User;
import com.suelake.habbo.util.SecurityUtil;

public class UPDATE implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// 'Decode' user object sent by client
		KeyValueStringReader obj = new KeyValueStringReader(msg.getBody(), "=");
		User usr = comm.getUserObject();
		
		// Apply changes
		if(obj.read("ph_figure", null) == null)
		{
			usr.password = obj.read("password");
			usr.email = obj.read("email");
			usr.dateOfBirth = obj.read("birthday");
			usr.countryID = (short)obj.readInt("country");
			usr.regionID = (short)obj.readInt("region");
			usr.phoneNumber = obj.read("phoneNumber");
			usr.motto = SecurityUtil.filterInput(obj.read("customData"));
			usr.figure = obj.read("figure");
			usr.sex = (obj.read("sex").equals("M")) ? 'M' : 'F';
		}
		else
		{
			usr.poolFigure = obj.read("ph_figure");
		}
		
		// TODO: verify data, filter scripting stuff
		
		// Update user object
		HabboHotel.getUserRegister().updateUser(usr);
		
		// Re-send user object
		comm.getRequestHandlers().callHandler(ClientCommands.INFORETRIEVE);
	}
}
