package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.blunk.util.KeyValueStringReader;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.users.User;
import com.suelake.habbo.util.SecurityUtil;

public class REGISTER implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Registration limit
		final int regLimit = HabboHotel.getPropBox().getInt("user.reglimit", 1);
		Vector<String> names = HabboHotel.getAccessControl().getRegisteredUsers(comm.getConnection().getIpAddress());
		if(names.size() >= regLimit)
		{
			comm.response.set("SYSTEMBROADCAST");
			comm.response.appendNewArgument("Sorry, but you cannot register a new user.");
			comm.response.appendNewArgument("We only tolerate " + regLimit + " user(s) per house.\r");
			comm.response.appendNewArgument("You already have the following user(s) registered:");
			for(String name : names)
			{
				comm.response.appendNewArgument("- " + name);
			}
			comm.sendResponse();
			return;
		}
		
		// 'Decode' user object sent by client
		KeyValueStringReader obj = new KeyValueStringReader(msg.getBody(), "=");
		
		// Create User object for database implementation (storage)
		User usr = HabboHotel.getUserRegister().newUser();
		
		// Misconfiguration error, but it's logged already
		if (usr != null)
		{
			// 'Fill' the User object that will be stored in the database
			usr.name = obj.read("name");
			usr.password = obj.read("password");
			
			// Personal
			usr.email = obj.read("email");
			usr.dateOfBirth = obj.read("birthday");
			usr.countryID = (short)obj.readInt("country");
			usr.regionID = (short)obj.readInt("region");
			usr.phoneNumber = obj.read("phoneNumber");
			usr.registered = TimeHelper.getDateTime();
			
			// Avatar
			usr.motto = SecurityUtil.filterInput(obj.read("customData"));
			usr.figure = obj.read("figure");
			usr.sex = (obj.read("sex").equals("Male")) ? 'M' : 'F';
			
			// Try to register user (sets default data and verifies given data)
			if (HabboHotel.getUserRegister().registerUser(usr, comm.getConnection().getIpAddress()))
			{
				comm.response.set("REGOK");
				comm.sendResponse();
			}
			else
			{
				comm.systemError("Invalid registration data, please try again.");
			}
		}
	}
}
