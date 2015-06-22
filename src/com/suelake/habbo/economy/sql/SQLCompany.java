package com.suelake.habbo.economy.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.economy.Company;

public class SQLCompany extends Company implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE FROM economy_companies WHERE id = ?;");
		query.setString(1, super.ID);
		query.executeUpdate();
		query.close();
		
		return true;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("INSERT INTO economy_companies(id,displayname,scale,risk,funds) VALUES (?,?,?,?,?)");
		query.setString(1, super.ID);
		query.setString(2, super.displayName);
		query.setFloat(3, super.scale);
		query.setFloat(4, super.scale);
		query.setInt(5, super.funds);
		boolean inserted = (query.executeUpdate() != 0);
		query.close();
		
		return inserted;
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("SELECT * FROM economy_companies WHERE id = ?;");
		query.setString(1, super.ID);;
		boolean loaded = SQLCompany.parseFromResultSet(this, query.executeQuery());
		query.close();
		
		return loaded;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("UPDATE economy_companies SET displayname = ?,scale = ?,risk = ?,funds = ? WHERE id = ?;");
		query.setString(1, super.displayName);
		query.setFloat(2, super.scale);
		query.setFloat(3, super.risk);
		query.setInt(4, super.funds);
		query.setString(5, super.ID);
		query.executeUpdate();
		query.close();
		
		return true;
	}
	
	public static boolean parseFromResultSet(SQLCompany obj, ResultSet result) throws SQLException
	{
		if (result.next())
		{
			obj.displayName = result.getString("displayname");
			obj.scale = result.getFloat("scale");
			obj.risk = result.getFloat("risk");
			obj.funds = result.getInt("funds");
			return true;
		}
		else
		{
			return false;
		}
	}
}
