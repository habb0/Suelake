package com.suelake.habbo.messenger.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.messenger.MessengerBuddy;
import com.suelake.habbo.messenger.MessengerBuddyGrabber;

public class SQLMessengerBuddyGrabber extends MessengerBuddyGrabber implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<MessengerBuddy> query(Connection conn) throws SQLException
	{
		// Prepare query to load the messenger_buddylist entries where accepted = 1
		/* This query was written by Mark Bertels aka Dissi, thanks man. A query with IN() takes over 3 seconds even on a small buddy list, this one is awesome fast */
		PreparedStatement query = conn
				.prepareStatement("SELECT u.id AS id,u.name AS name,u.figure AS figure,u.sex AS sex,u.motto_messenger AS motto_messenger,u.lastactivity AS lastactivity FROM messenger_buddylist INNER JOIN users AS u ON u.id = userid WHERE buddyid = ? AND accepted = 1 UNION ALL SELECT u.id AS id,u.name AS name,u.figure AS figure,u.sex AS sex,u.motto_messenger AS motto_messenger,u.lastactivity AS lastactivity FROM messenger_buddylist INNER JOIN users AS u ON u.id = buddyid WHERE userid = ? AND accepted = 1;");
		query.setInt(1, super.userID);
		query.setInt(2, super.userID);
		
		// Execute query
		ResultSet result = query.executeQuery();
		
		// Fetch results
		Vector<MessengerBuddy> buddies = new Vector<MessengerBuddy>(5);
		while (result.next())
		{
			MessengerBuddy buddy = SQLMessengerBuddy.parse(result);
			if (buddy != null)
			{
				buddies.add(buddy);
			}
		}
		
		query.close();
		
		// Return the result
		return buddies;
	}
}
