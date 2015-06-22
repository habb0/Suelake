package com.suelake.habbo.items.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.items.ItemLoader;

public class SQLItemLoader extends ItemLoader implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public Vector<Item> query(Connection conn) throws SQLException
	{
		PreparedStatement query = null;
		if(super.userID > 0)
		{
			if(super.spaceID == 0)
			{
				// Load from inventory
				query = conn.prepareStatement("SELECT id,definitionid,ownerid,customdata,teleporterid,itemdata FROM items WHERE ownerid = ? AND spaceid = 0 ORDER BY id;");
				query.setInt(1, super.userID);
			}
		}
		else if(super.spaceID > 0)
		{
			// Load from space
			query = conn.prepareStatement("SELECT * FROM items WHERE spaceid = ?;");
			query.setInt(1, super.spaceID);
		}
		
		// Execute query
		ResultSet result = query.executeQuery();
		
		// Fetch results
		Vector<Item> items = new Vector<Item>();
		while(result.next())
		{
			Item item = new SQLItem();
			if(SQLItem.parseFromResultSet(item, result, (super.spaceID > 0)))
			{
				items.add(item);
			}
		}
		
		query.close();
		
		// Return bucket
		return items;
	}
}
