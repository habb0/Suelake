package com.suelake.habbo.items;

import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Map.Entry;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;

/**
 * ItemStripHandler holds the Items of a User while in a SpaceInstance. \r
 * ItemStripHandler provides methods for refreshing the 'hand' and trading.
 * 
 * @author Nillus
 */
public class ItemInventoryHandler
{
	private static final int ITEMSTRIP_ITEMS_ON_PAGE = 9;
	
	// Client
	private CommunicationHandler m_comm;
	
	// Item strip
	private LinkedHashMap<Integer, Item> m_items;
	private byte m_stripPage;
	private boolean m_stripItemsLoaded;
	
	public ItemInventoryHandler(CommunicationHandler comm)
	{
		m_comm = comm;
		m_items = new LinkedHashMap<Integer, Item>();
	}
	
	public void clear()
	{
		m_items.clear();
		m_stripPage = 0;
		m_stripItemsLoaded = false;
	}
	
	public void loadStripItems()
	{
		if (!m_stripItemsLoaded)
		{
			Vector<Item> items = HabboHotel.getItemAdmin().getUserItemInventory(m_comm.getUserObject().ID);
			for(Item item : items)
			{
				m_items.put(item.ID, item);
			}
			m_stripItemsLoaded = true;
		}
	}
	
	public void addItem(Item item)
	{
		m_items.put(item.ID, item);
	}
	
	public void removeItem(int itemID)
	{
		m_items.remove(itemID);
	}
	
	public boolean containsItem(int itemID)
	{
		return m_items.containsKey(itemID);
	}
	
	public Item getItem(int itemID)
	{
		return m_items.get(itemID);
	}
	
	public void resetStripPage()
	{
		m_stripPage = 0;
	}
	
	public void sendStrip(String mode)
	{
		if (mode.equals("new"))
		{
			m_stripPage = 0;
		}
		else if (mode.equals("next"))
		{
			m_stripPage++;
		}
		else if (mode.equals("last"))
		{
			m_stripPage = (byte)((m_items.size() - 1) / ITEMSTRIP_ITEMS_ON_PAGE);
		}
		
		ServerMessage msg = new ServerMessage("STRIPINFO ");
		int start = 0;
		int end = m_items.size();
		
		// Items in hand?
		if (end > 0)
		{
			start = m_stripPage * ITEMSTRIP_ITEMS_ON_PAGE;
			if (end > (start + ITEMSTRIP_ITEMS_ON_PAGE))
			{
				end = start + ITEMSTRIP_ITEMS_ON_PAGE;
			}
			while (start >= end)
			{
				m_stripPage--;
				start = m_stripPage * ITEMSTRIP_ITEMS_ON_PAGE;
				if (end > (start + ITEMSTRIP_ITEMS_ON_PAGE))
				{
					end = start + ITEMSTRIP_ITEMS_ON_PAGE;
				}
			}
			
			// Serialize items
			int index = 0;
			for(Entry<Integer, Item> pair : m_items.entrySet())
			{
				if(index >= start && index < end)
				{
					pair.getValue().serialize(msg, index);
				}
				index++;
			}
		}
		
		// Total amount of items
		msg.appendNewArgument(Integer.toString(m_items.size()));
		
		// Send to client
		m_comm.sendMessage(msg);
	}
	
	public int count()
	{
		return m_items.size();
	}
	
	public CommunicationHandler getCommunicator()
	{
		return m_comm;
	}
}
