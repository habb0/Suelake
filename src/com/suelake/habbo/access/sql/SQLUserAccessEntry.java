package com.suelake.habbo.access.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.blunk.storage.sql.SQLDataObject;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.access.UserAccessEntry;

/**
 * SQLUserAccessEntry is a UserAccessEntry that is stored in a SQL database.
 * 
 * @author Nillus
 */
public class SQLUserAccessEntry extends UserAccessEntry implements SQLDataObject
{
	
	@Override
	public long getCacheKey()
	{
		return super.ID;
	}
	
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE FROM users_access WHERE id = ?;");
		query.setInt(1, super.ID);
		
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn.prepareStatement("INSERT INTO users_access(userid,ip,login,registration) VALUES (?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		query.setInt(1, super.userID);
		query.setString(2, super.ip);
		query.setTimestamp(3, new Timestamp(super.login.getTime()));
		query.setBoolean(4, super.isRegistration);
		
		// Execute
		query.executeUpdate();
		
		// And get latest inserted ID
		ResultSet keys = query.getGeneratedKeys();
		
		if(keys.next())
		{
			super.ID = keys.getInt(1);
			
			// Close the recordset
			query.close();
			
			return true;
		}
		
		// Close the recordset
		query.close();
		
		// Insertion failed!
		return false;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn.prepareStatement("UPDATE users_access SET logout = ? WHERE id = ?;");
		
		// Set the params
		query.setTimestamp(1, new Timestamp(super.logout.getTime()));
		query.setInt(2, super.ID);
		
		// We need to safely close this!
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		// Execute
		return (result);
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = null;
		if (super.userID > 0)
		{
			query = conn.prepareStatement("SELECT * FROM users_access WHERE userid = ? ORDER BY id DESC LIMIT 1;");
			query.setInt(1, super.userID);
		}
		
		// Execute
		ResultSet result = query.executeQuery();
		
		// Results found?
		if (result.next())
		{
			// Parse result
			super.ID = result.getInt("id");
			super.userID = result.getInt("userid");
			super.ip = result.getString("ip");
			super.login = new Date(result.getTimestamp("login").getTime());
			try
			{
				super.logout = new Date(result.getTimestamp("logout").getTime());
			}
			catch (Exception ex)
			{
				// Probably not logged out (yet)!
				super.logout = TimeHelper.getDateTime();
			}
			super.isRegistration = result.getBoolean("registration");
			
			// Close Resultset
			query.close();
			
			return true;
		}
		
		// Close Resultset
		query.close();
		
		// No results / failed to parse
		return false;
	}
}
