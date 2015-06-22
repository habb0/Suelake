package com.suelake.habbo.items;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.Log;
import com.suelake.habbo.HabboHotel;

/**
 * ItemDefinitionManager stores ItemDefinitions in a collection and provides methods for accessing the collection.
 * 
 * @author Nillus
 */
public class ItemDefinitionManager
{
	private Vector<ItemDefinition> m_definitions;
	
	public ItemDefinitionManager()
	{
		m_definitions = new Vector<ItemDefinition>(150);
	}
	
	/**
	 * Attempts to find a ItemDefinition with a given ID.
	 * 
	 * @param definitionID The ID of the ItemDefinition to get.
	 * @return The ItemDefinition object if the definition is found, or NULL when it was not present in the collection.
	 */
	public ItemDefinition getDefinition(int definitionID)
	{
		for (ItemDefinition def : m_definitions)
		{
			if (def.ID == definitionID)
			{
				return def;
			}
		}
		
		return null;
	}
	
	private final static String[] PRESENTBOX_SPRITES = { "present_gen1", "present_gen2", "present_gen3", "present_gen4", "present_gen5", "present_gen6" };
	public ItemDefinition getPresentBoxDefinition()
	{
		String sprite = PRESENTBOX_SPRITES[Environment.getRandom().nextInt(PRESENTBOX_SPRITES.length)];
		
		for(ItemDefinition def : m_definitions)
		{
			if(def.sprite.equals(sprite))
			{
				return def;
			}
		}
		
		return null;
	}
	
	public ItemDefinition getPhotoDefinition()
	{
		for(ItemDefinition def : m_definitions)
		{
			if(def.behaviour.isPhoto)
			{
				return def;
			}
		}
		
		return null;
	}
	
	/**
	 * Clears the ItemDefinition collection.
	 */
	public void clear()
	{
		m_definitions.clear();
	}
	
	/**
	 * Attempts to load all ItemDefinitions from the Database.
	 * 
	 * @return The amount of items that was loaded.
	 */
	@SuppressWarnings("unchecked")
	public int loadDefinitions()
	{
		// Clear current definitions
		this.clear();
		Log.info("Loading item definitions...");
		
		// Create query and execute it
		ItemDefinitionLoader loader = (ItemDefinitionLoader)HabboHotel.getDataQueryFactory().newQuery("ItemDefinitionLoader");
		m_definitions = (Vector<ItemDefinition>)Environment.getDatabase().query(loader);
		
		// Return amount of loaded definitions
		Log.info("Loaded " + m_definitions.size() + " item definitions.");
		return m_definitions.size();
	}
}
