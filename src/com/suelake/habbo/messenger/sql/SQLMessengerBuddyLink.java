package com.suelake.habbo.messenger.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.messenger.MessengerBuddyLink;

public class SQLMessengerBuddyLink extends MessengerBuddyLink implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		Statement query = conn.createStatement();
		boolean deleted = (query.executeUpdate("DELETE FROM messenger_buddylist WHERE (userid = " + super.ID1 + " AND buddyid = " + super.ID2 + ") OR (userid = " + super.ID2 + " AND buddyid = " + super.ID1 + ") LIMIT 1;") > 0);
		query.close();
		
		return deleted;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("INSERT INTO messenger_buddylist(userid,buddyid,accepted) VALUES (?,?,?);");
		query.setInt(1, super.ID1);
		query.setInt(2, super.ID2);
		query.setBoolean(3, super.isAccepted);
		boolean inserted = (query.executeUpdate() > 0);
		query.close();
		
		return inserted;
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("SELECT * FROM messenger_buddylist WHERE (userid = ? AND buddyid = ?) OR (buddyid = ? AND userid = ?) LIMIT 1;");
		
		// userID > buddyID
		query.setInt(1, super.ID1);
		query.setInt(2, super.ID2);
		
		// buddyID > userID
		query.setInt(3, super.ID1);
		query.setInt(4, super.ID2);
		
		ResultSet result = query.executeQuery();
		if(result.next())
		{
			super.isAccepted = result.getBoolean("accepted");
			query.close();
			
			return true;
		}
		else
		{
			query.close();
			return false;
		}
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("UPDATE messenger_buddylist SET accepted = ? WHERE (userid = ? AND buddyid = ?) OR (buddyid = ? AND userid = ?) LIMIT 1;");
		query.setBoolean(1, super.isAccepted);
		query.setInt(2, super.ID1);
		query.setInt(3, super.ID2);
		query.setInt(4, super.ID1);
		query.setInt(5, super.ID2);
		
		boolean updated = (query.executeUpdate() > 0);
		query.close();
		
		return updated;
	}
	
}
