package com.suelake.habbo.messenger.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.blunk.Log;
import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.messenger.MessengerMessage;

public class SQLMessengerMessage extends MessengerMessage implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		conn.createStatement().executeUpdate("UPDATE messenger_messages SET isread = 0x01 WHERE id = " + super.ID + ";");
		return true;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn.prepareStatement("INSERT INTO messenger_messages(senderid,receiverid,timestamp,text) VALUES (?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		query.setInt(1, super.senderID);
		query.setInt(2, super.receiverID);
		query.setTimestamp(3, new Timestamp(super.timestamp.getTime()));
		query.setString(4, text);
		
		// Execute
		query.executeUpdate();
		
		// Get latest inserted ID
		ResultSet keys = query.getGeneratedKeys();
		
		if (keys.next())
		{
			super.ID = keys.getInt(1);
			query.close();
			return true;
		}
		
		query.close();
		
		// Failed to insert...
		return false;
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		return false;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		return false;
	}
	
	public static MessengerMessage parse(ResultSet result)
	{
		MessengerMessage msg = new SQLMessengerMessage();
		try
		{
			msg.ID = result.getInt("id");
			msg.senderID = result.getInt("senderid");
			msg.receiverID = result.getInt("receiverid");
			msg.timestamp = new Date(result.getTimestamp("timestamp").getTime());
			msg.text = result.getString("text");
			msg.read = result.getBoolean("isread");
			msg.senderFigure = result.getString("figure");
			
			return msg;
		}
		catch(SQLException ex)
		{
			Log.error("Failed to parse SQLMessengerMessage from java.sql.ResultSet, probably fields missing or data corrupt", ex);
		}
		
		return null;
	}
}
