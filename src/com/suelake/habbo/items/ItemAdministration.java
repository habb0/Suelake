package com.suelake.habbo.items;

import java.util.Vector;

import com.blunk.Environment;
import com.suelake.habbo.HabboHotel;

/**
 * ItemAdministration is a manager for Items.
 * 
 * @author Nillus
 */
public class ItemAdministration
{
	private Class<Item> m_itemClass;
	private ItemDefinitionManager m_defMgr;
	
	@SuppressWarnings("unchecked")
	public ItemAdministration()
	{
		// Get Item DataObject
		Item sample = (Item)HabboHotel.getDataObjectFactory().newObject("Item");
		if (sample != null)
		{
			Class rawClass = sample.getClass();
			m_itemClass = rawClass;
		}
		
		m_defMgr = new ItemDefinitionManager();
	}
	
	public Item getItem(int itemID)
	{
		Item item = this.newItem();
		item.ID = itemID;
		
		// Complete DataObject with data from Database
		if (Environment.getDatabase().load(item))
		{
			return item;
		}
		else
		{
			// No results / failed to parse
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Item> getUserItemInventory(int userID)
	{
		ItemLoader loader = (ItemLoader)HabboHotel.getDataQueryFactory().newQuery("ItemLoader");
		loader.userID = userID;
		loader.spaceID = 0;
		
		return (Vector<Item>)Environment.getDatabase().query(loader);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Item> getSpaceItems(int spaceID)
	{
		ItemLoader loader = (ItemLoader)HabboHotel.getDataQueryFactory().newQuery("ItemLoader");
		loader.userID = 0;
		loader.spaceID = spaceID;
		
		return (Vector<Item>)Environment.getDatabase().query(loader);
	}
	
	public boolean storeItem(Item item)
	{
		if (item != null)
		{
			return Environment.getDatabase().insert(item);
		}
		else
		{
			return false;
		}
	}
	
	public boolean updateItem(Item item)
	{
		if (item != null)
		{
			return Environment.getDatabase().update(item);
		}
		else
		{
			return false;
		}
	}
	
	public boolean deleteItem(Item item)
	{
		if (item != null)
		{
			if (item.definition.behaviour.isPhoto)
			{
				HabboHotel.getPhotoService().deletePhoto(Integer.parseInt(item.customData));
			}
			
			return Environment.getDatabase().delete(item);
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Creates a new instance of the Item DataObject implementation class and returns it.
	 */
	public Item newItem()
	{
		try
		{
			return m_itemClass.newInstance();
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns the instance of the ItemDefinitionManager.
	 */
	public ItemDefinitionManager getDefinitions()
	{
		return m_defMgr;
	}
}
