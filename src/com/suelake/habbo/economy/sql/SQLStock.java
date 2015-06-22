package com.suelake.habbo.economy.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.economy.Stock;

public class SQLStock extends Stock implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE FROM economy_stocks WHERE id = ?;");
		query.setInt(1, super.ID);
		query.executeUpdate();
		query.close();
		
		return true;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("INSERT INTO economy_stocks(companyid,userid,credits,moment) VALUES (?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
		query.setString(1, super.companyID);
		query.setInt(2, super.userID);
		query.setShort(3, super.credits);
		query.setTimestamp(4, new Timestamp(super.moment.getTime()));
		query.executeUpdate();
		
		// Attempt to parse the generated keys and set it as the ID of this DataObject
		ResultSet keys = query.getGeneratedKeys();
		if(keys.next())
		{
			super.ID = keys.getInt(1);
		}
		query.close();
		
		return (super.ID != 0);
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("SELECT * FROM economy_stocks WHERE id = ?;");
		query.setInt(1, super.ID);
		
		ResultSet result = query.executeQuery();
		boolean exists = result.next();
		if(exists) SQLStock.parseFromResultSet(this, result);
		query.close();
		
		return exists;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		return false;
	}
	
	public static boolean parseFromResultSet(SQLStock obj, ResultSet result) throws SQLException
	{
		obj.companyID = result.getString("companyid");
		obj.userID = result.getInt("userid");
		obj.credits = result.getShort("credits");
		obj.moment = new Date(result.getTimestamp("moment").getTime());
		return true;
	}
}
