package com.suelake.habbo.items.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.items.Item;

public class SQLItem extends Item implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE FROM items WHERE id = ?;");
		query.setInt(1, super.ID);
		
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	public static boolean parseFromResultSet(Item item, ResultSet result, boolean parseInSpaceData) throws SQLException
	{
		if (item.setDefinition(result.getInt("definitionid")))
		{
			item.ID = result.getInt("id");
			item.ownerID = result.getInt("ownerid");
			item.customData = result.getString("customdata");
			item.teleporterID = result.getInt("teleporterid");
			item.itemData = result.getString("itemdata");
			if (parseInSpaceData)
			{
				item.spaceID = result.getInt("spaceid");
				item.X = result.getShort("x");
				item.Y = result.getShort("y");
				item.Z = result.getFloat("z");
				item.rotation = result.getByte("rotation");
				item.wallPosition = result.getString("wallposition");
			}
			
			// Parsed OK!
			return true;
		}
		
		// Failed!
		return false;
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("INSERT INTO items(definitionid,ownerid,customdata,teleporterid,itemdata) VALUES (?,?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		query.setInt(1, super.definition.ID);
		query.setInt(2, super.ownerID);
		if (super.customData == null)
		{
			query.setNull(3, Types.VARCHAR);
		}
		else
		{
			query.setString(3, super.customData);
		}
		query.setInt(4, super.teleporterID);
		if (super.itemData == null)
		{
			query.setNull(5, Types.VARCHAR);
		}
		else
		{
			query.setString(5, super.itemData);
		}
		
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
		
		// Insertion failed!
		query.close();
		return false;
	}
	
	@Override
	public boolean load(Connection conn) throws SQLException
	{
		// Prepare query
		PreparedStatement query = conn.prepareStatement("SELECT * FROM items WHERE id = ?;");
		query.setInt(1, super.ID);
		
		// Get result
		ResultSet result = query.executeQuery();
		
		if (result.next())
		{
			if (SQLItem.parseFromResultSet(this, result, true))
			{
				query.close();
				return true;
			}
		}
		
		// No results, could not complete data
		query.close();
		
		return false;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("UPDATE items SET definitionid = ?,ownerid = ?,spaceid = ?,x = ?,y = ?,z = ?,rotation=?,customdata = ?,teleporterid = ?,wallposition = ?,itemdata = ? WHERE id = ?;");
		query.setInt(1, super.definition.ID);
		query.setInt(2, super.ownerID);
		query.setInt(3, super.spaceID);
		query.setShort(4, super.X);
		query.setShort(5, super.Y);
		query.setFloat(6, super.Z);
		query.setByte(7, super.rotation);
		
		if (super.customData == null)
		{
			query.setNull(8, Types.VARCHAR);
		}
		else
		{
			query.setString(8, super.customData);
		}
		
		query.setInt(9, super.teleporterID);
		
		if (super.wallPosition == null)
		{
			query.setNull(10, Types.VARCHAR);
		}
		else
		{
			query.setString(10, super.wallPosition);
		}
		
		if (super.itemData == null)
		{
			query.setNull(11, Types.VARCHAR);
		}
		else
		{
			query.setString(11, super.itemData);
		}
		
		query.setInt(12, super.ID);
		
		// And execute the query
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	@Override
	public long getCacheKey()
	{
		return super.ID;
	}
	
}
