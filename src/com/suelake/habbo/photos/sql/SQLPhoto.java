package com.suelake.habbo.photos.sql;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.blunk.storage.sql.SQLDataObject;
import com.suelake.habbo.photos.Photo;

public class SQLPhoto extends Photo implements SQLDataObject
{
	@Override
	public boolean delete(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("DELETE FROM items_photos WHERE id = ?;");
		query.setInt(1, super.ID);
		
		boolean result = (query.executeUpdate() > 0);
		query.close();
		
		return (result);
	}
	
	@Override
	public boolean insert(Connection conn) throws SQLException
	{
		// Create the BLOB for the binary image data
		Blob imageBlob = conn.createBlob();
		imageBlob.setBytes(1, super.image);
		
		// Prepare the query
		PreparedStatement query = conn.prepareStatement("INSERT INTO items_photos(stime,cs,image) VALUES (?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		query.setString(1, super.time);
		query.setInt(2, super.cs);
		query.setBlob(3, imageBlob);
		
		// Insert the data and try to get latest inserted ID
		query.executeUpdate();
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
		PreparedStatement query = conn.prepareStatement("SELECT stime,cs,image FROM items_photos WHERE id = ?;");
		query.setInt(1, super.ID);
		
		// Execute
		ResultSet result = query.executeQuery();
		
		if (result.next())
		{
			super.time = result.getString(1);
			super.cs = result.getInt(2);
			
			// Unwrap image bytes from the Blob
			Blob imageBlob = result.getBlob(3);
			super.image = imageBlob.getBytes(1, (int)imageBlob.length());
			
			// OK
			query.close();
			return true;
		}
		
		// No result
		query.close();
		return false;
	}
	
	@Override
	public boolean update(Connection conn) throws SQLException
	{
		return false;
	}
	
	@Override
	public long getCacheKey()
	{
		return this.ID;
	}
}
