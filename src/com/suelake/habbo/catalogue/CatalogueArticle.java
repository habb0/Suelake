package com.suelake.habbo.catalogue;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.items.ItemDefinition;

/**
 * CatalogueArticle represents an article on a CataloguePage in the Catalogue.\r
 * CatalogueArticles are unique in the Catalogue by their 'article ID'.
 * 
 * @author Nillus
 */
public class CatalogueArticle implements SerializableObject
{
	/**
	 * The unique ID of this article. (bar code)
	 */
	public int ID;
	/**
	 * The ID of the catalogue page this article appears on.
	 */
	public int pageID;
	/**
	 * The price of this article in Credits.
	 */
	public int price;
	
	/**
	 * The actual ItemDefinition representing the item this article 'ships'.
	 */
	private ItemDefinition m_innerItem;
	public int posterID = 0;
	
	/**
	 * Returns the ItemDefinition of the inner item in this article.
	 */
	public ItemDefinition getItem()
	{
		return m_innerItem;
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendKV2Argument("p", m_innerItem.name);
		msg.appendTabArgument(m_innerItem.description);
		msg.appendTabArgument(Integer.toString(this.price));
		msg.appendTabArgument("");
		
		// Article type
		if (m_innerItem == null)
		{
			msg.appendTabArgument("d"); // Package
		}
		else
		{
			if (m_innerItem.behaviour.STUFF)
			{
				msg.appendTabArgument("s");
			}
			else if (m_innerItem.behaviour.ITEM)
			{
				msg.appendTabArgument("i");
			}
		}
		
		// Article sprite
		if (m_innerItem == null)
		{
			msg.appendTabArgument(""); // Package
		}
		else
		{
			if (this.posterID == 0)
			{
				msg.appendTabArgument(m_innerItem.sprite);
			}
			else
			{
				msg.appendTabArgument(m_innerItem.sprite + " " + posterID);
			}
		}
		
		// ???
		if (m_innerItem == null || !m_innerItem.behaviour.STUFF)
		{
			msg.appendTabArgument("");
		}
		else
		{
			msg.appendTabArgument("0");
		}
		
		// Length of STUFF
		if (m_innerItem == null || !m_innerItem.behaviour.STUFF)
		{
			msg.appendTabArgument("");
		}
		else
		{
			msg.appendTabArgument(m_innerItem.length + "," + m_innerItem.width);
		}
		
		msg.appendTabArgument(Integer.toString(this.ID));
		if (m_innerItem == null || (m_innerItem.behaviour.ITEM && m_innerItem.sprite.equals("poster")))
		{
			msg.appendTabArgument("");
		}
		if (m_innerItem == null)
		{
			// PACKAGE: amount of item definitions
			// FOR EACH ITEM
			// SPRITE (posters: 'poster <id>')
			// AMOUNT OF THIS ITEM
			// COLOR OF THIS ITEM
		}
		else
		{
			if (m_innerItem.behaviour.STUFF)
			{
				msg.appendTabArgument(m_innerItem.color);
			}
		}
	}

	public boolean setInnerItem(int definitionID)
	{
		m_innerItem = HabboHotel.getItemAdmin().getDefinitions().getDefinition(definitionID);
		return (m_innerItem != null);
	}
}
