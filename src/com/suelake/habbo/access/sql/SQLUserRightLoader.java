package com.suelake.habbo.access.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.access.UserRightLoader;

public class SQLUserRightLoader extends UserRightLoader implements SQLDataQuery
{
	
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<String> query(Connection conn) throws SQLException
	{
		// Load rights from users_rights for this and lower roles
		PreparedStatement query = conn.prepareStatement("SELECT userright FROM users_rights WHERE role <= ? ORDER BY role ASC;");
		query.setByte(1, super.roleID);
		
		// Execute query
		ResultSet result = query.executeQuery();
		
		// Gather results
		Vector<String> rights = new Vector<String>();
		while (result.next())
		{
			String right = result.getString("userright");
			if(right != null)
			{
				rights.add(right);
			}
		}
		
		// Close query + resultset
		query.close();
		
		// Return the results
		return rights;
	}	
}
