package com.suelake.habbo.messenger.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.DatabaseException;
import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.messenger.MessengerMessage;
import com.suelake.habbo.messenger.MessengerMessageGrabber;

public class SQLMessengerMessageGrabber extends MessengerMessageGrabber implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws DatabaseException
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Vector<MessengerMessage> query(Connection conn) throws SQLException
	{
		// Prepare query to load the messenger messages where read = 0 + figures of senders from 'users'
		PreparedStatement query = conn.prepareStatement("SELECT messenger_messages.*,users.figure FROM messenger_messages INNER JOIN users ON messenger_messages.senderid = users.id WHERE receiverid = ? AND isread = 0;");
		query.setInt(1, super.userID);
		
		// Execute query
		ResultSet result = query.executeQuery();
		
		// Gather results
		Vector<MessengerMessage> msgs = new Vector<MessengerMessage>(3);
		while (result.next())
		{
			MessengerMessage msg = SQLMessengerMessage.parse(result);
			if (msg != null)
			{
				msgs.add(msg);
			}
		}
		
		query.close();
		
		// Return the results
		return msgs;
	}
}
