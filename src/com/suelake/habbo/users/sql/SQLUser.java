package com.suelake.habbo.users.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.blunk.Log;
import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.users.User;

public class SQLUser extends User implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE * FROM users WHERE id = ?;");
		query.setInt(1, super.ID);
		
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	private void setUserParams(PreparedStatement query) throws SQLException
	{
		// Account
		query.setString(1, super.name);
		query.setString(2, super.password);
		
		// Personal
		query.setString(3, super.email);
		query.setString(4, super.dateOfBirth);
		query.setShort(5, super.countryID);
		query.setShort(6, super.regionID);
		query.setString(7, super.phoneNumber);
		query.setTimestamp(8, new Timestamp(super.registered.getTime()));
		
		// Privileges
		query.setByte(9, super.role);
		
		// Avatar
		query.setString(10, super.motto);
		query.setString(11, super.figure);
		query.setString(12, Character.toString(super.sex));
		query.setString(13, super.poolFigure);
		
		// Valueables
		query.setShort(14, super.credits);
		query.setShort(15, super.film);
		query.setShort(16, super.gameTickets);
		
		// Misc
		query.setString(17, super.messengerMotto);
		query.setTimestamp(18, new Timestamp(super.lastActivity.getTime()));
		
		// HC
		query.setShort(19, super.hcDaysTotal);
		query.setShort(20, super.hcDaysExpired);
		query.setTimestamp(21, new Timestamp(super.hcLastUpdate.getTime()));
	}
	
	private static boolean parseFromResultSet(User usr, ResultSet result)
	{
		try
		{
			// Account
			usr.ID = result.getInt("id");
			usr.name = result.getString("name");
			usr.password = result.getString("password");
			
			// Personal
			usr.email = result.getString("email");
			usr.dateOfBirth = result.getString("dob");
			usr.countryID = result.getShort("country");
			usr.regionID = result.getShort("region");
			usr.phoneNumber = result.getString("phonenumber");
			usr.registered = new Date(result.getTimestamp("registered").getTime());
			
			// Privileges
			usr.role = result.getByte("role");
			
			// Avatar
			usr.motto = result.getString("motto");
			usr.figure = result.getString("figure");
			usr.sex = (result.getString("sex").equals("M")) ? 'M' : 'F';
			usr.poolFigure = result.getString("poolfigure");
			
			// Valueables
			usr.credits = result.getShort("credits");
			usr.film = result.getShort("film");
			usr.gameTickets = result.getShort("gametickets");
			
			// Misc
			usr.messengerMotto = result.getString("motto_messenger");
			usr.lastActivity = new Date(result.getTimestamp("lastactivity").getTime());
			
			// HC
			usr.hcDaysTotal = result.getShort("hc_total");
			usr.hcDaysExpired = result.getShort("hc_expired");
			usr.hcLastUpdate = new Date(result.getTimestamp("hc_lastupdate").getTime());
			
			return true;
		}
		catch (Exception ex)
		{
			Log.error("Could not fully parse net.scriptomatic.habbo.users.User (SQLUser) from given java.sql.ResultSet, probably fields missing or bad data!", ex);
		}
		
		return false;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn
				.prepareStatement(
						"INSERT INTO users(name,password,email,dob,country,region,phonenumber,registered,role,motto,figure,sex,poolfigure,credits,film,gametickets,motto_messenger,lastactivity,hc_total,hc_expired,hc_lastupdate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);",
						PreparedStatement.RETURN_GENERATED_KEYS);
		this.setUserParams(query);
		
		// Execute
		query.executeUpdate();
		
		// And get the latest ID
		ResultSet keys = query.getGeneratedKeys();
		
		if (keys.next())
		{
			super.ID = keys.getInt(1);
			query.close();
			
			return true;
		}
		else
		{
			// Insertion failed!
			query.close();
			return false;
		}
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn
				.prepareStatement("UPDATE users SET name=?,password=?,email=?,dob=?,country=?,region=?,phonenumber=?,registered=?,role=?,motto=?,figure=?,sex=?,poolfigure=?,credits=?,film=?,gametickets=?,motto_messenger=?,lastactivity=?,hc_total=?,hc_expired=?,hc_lastupdate=? WHERE id = ?;");
		this.setUserParams(query);
		query.setInt(22, super.ID);
		
		// And execute
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		// Prepare the query
		PreparedStatement query;
		if (this.ID > 0)
		{
			// Load by ID
			query = conn.prepareStatement("SELECT * FROM users WHERE id = ?;");
			query.setInt(1, super.ID);
		}
		else
		{
			// Load by name
			query = conn.prepareStatement("SELECT * FROM users WHERE name = ?;");
			query.setString(1, super.name);
		}
		
		// Execute it and try to parse the result
		ResultSet result = query.executeQuery();
		
		if (result.next())
		{
			if (SQLUser.parseFromResultSet(this, result))
			{
				query.close();
				return true;
			}
		}
		
		// No results / failed to parse
		query.close();
		return false;
	}
	
	@Override
	public long getCacheKey()
	{
		return super.ID;
	}
}
