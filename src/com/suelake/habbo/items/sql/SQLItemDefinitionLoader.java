package com.suelake.habbo.items.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.Log;
import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.items.ItemBehaviour;
import com.suelake.habbo.items.ItemDefinition;
import com.suelake.habbo.items.ItemDefinitionLoader;

public class SQLItemDefinitionLoader extends ItemDefinitionLoader implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<ItemDefinition> query(Connection conn) throws SQLException
	{
		// Create query and execute
		ResultSet result = conn.createStatement().executeQuery("SELECT * FROM items_definitions ORDER BY id;");
		
		// Fetch results
		Vector<ItemDefinition> defs = new Vector<ItemDefinition>(150);
		while(result.next())
		{
			ItemDefinition def = parseDefinition(result);
			if(def != null)
			{
				defs.add(def);
			}
		}
		
		// Close resultSet
		result.close();
		
		// Return parsed results
		return defs;
	}
	
	private static ItemDefinition parseDefinition(ResultSet result)
	{
		ItemDefinition def = new ItemDefinition();
		try
		{
			def.ID = result.getInt("id");
			def.sprite = result.getString("sprite");
			def.color = result.getString("color");
			def.length = result.getByte("length");
			def.width = result.getByte("width");
			def.heightOffset = result.getFloat("height");
			def.name = result.getString("name");
			def.description = result.getString("description");
			def.customDataClass = result.getString("dataclass");
			if(def.customDataClass == null)
			{
				def.customDataClass = "NULL";
			}
			def.behaviour = ItemBehaviour.parse(result.getString("behaviour"));
			
			return def;
		}
		catch(Exception ex)
		{
			Log.error("Could not fully parse ItemDefinition from SQLDatabase query result!", ex);
		}
		
		return null;
	}
}
