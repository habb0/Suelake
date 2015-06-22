package com.suelake.habbo.messenger.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.blunk.Log;
import com.suelake.habbo.messenger.MessengerBuddy;

public abstract class SQLMessengerBuddy
{
	public static MessengerBuddy parse(ResultSet result)
	{
		MessengerBuddy buddy = new MessengerBuddy();
		try
		{
			buddy.ID = result.getInt("id");
			buddy.name = result.getString("name");
			buddy.figure = result.getString("figure");
			buddy.sex = (result.getString("sex").equals("M") ? 'M' : 'F');
			buddy.messengerMotto = result.getString("motto_messenger");
			buddy.lastActivity = result.getDate("lastactivity");
			
			return buddy;
		}
		catch(SQLException ex)
		{
			Log.error("Failed to parse MessengerBuddy from java.sql.ResultSet, probably fields missing or data corrupt", ex);
		}
		
		return buddy;
	}
}
