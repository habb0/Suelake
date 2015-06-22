package com.suelake.habbo.messenger.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.messenger.MessengerBuddyRequestGrabber;

public class SQLMessengerBuddyRequestGrabber extends MessengerBuddyRequestGrabber implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
	}
	
	@Override
	public Vector<String> query(Connection conn) throws SQLException
	{
		// Prepare query to load the buddy entries where accepted = 0
		PreparedStatement query = conn.prepareStatement("SELECT u.name FROM messenger_buddylist AS mb JOIN users AS u ON u.id = mb.userid WHERE mb.buddyid = ? AND accepted = 0;");
		query.setInt(1, super.userID);
		
		// Execute query
		ResultSet result = query.executeQuery();
		
		// Gather the results (names of users that have sent a buddy request to this user)
		Vector<String> names = new Vector<String>(3);
		while (result.next())
		{
			names.add(result.getString(1)); // "name"
		}
		
		query.close();
		
		// Return the result
		return names;
	}
}
