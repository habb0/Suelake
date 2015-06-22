package com.suelake.habbo.access.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.access.UserRegistrationFinder;

public class SQLUserRegistrationFinder extends UserRegistrationFinder implements SQLDataQuery
{
	@Override
	public Vector<?> query(Connection conn) throws SQLException
	{
		// Execute query
		PreparedStatement query = conn.prepareStatement("SELECT users.name FROM users_access JOIN users ON users.id = users_access.userid WHERE ip = ? AND registration = ?;");
		query.setString(1, super.ip);
		query.setBoolean(2, true);
		ResultSet result = query.executeQuery();
		
		// Parse results
		Vector<String> names = new Vector<String>(1);
		while(result.next())
		{
			names.add(result.getString("name"));
		}
		return names;
	}

	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
}
