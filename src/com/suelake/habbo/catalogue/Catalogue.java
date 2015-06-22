package com.suelake.habbo.catalogue;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.Log;
import com.blunk.storage.DataQuery;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

/**
 * The Catalogue is an ingame 'catalogue' consisting out of pages with articles (such as furniture) on them.\r
 * Articles can be purchased by Users at the expense of credits.
 * 
 * @author Nillus
 */
public class Catalogue
{
	private Vector<CataloguePage> m_pages;
	private Vector<CatalogueArticle> m_articles;
	
	public Catalogue()
	{
		m_pages = new Vector<CataloguePage>();
		m_articles = new Vector<CatalogueArticle>();
	}
	
	/**
	 * Clears the CataloguePage collection and loads the CataloguePages from the Database.
	 */
	@SuppressWarnings("unchecked")
	public void loadPages()
	{
		DataQuery query = HabboHotel.getDataQueryFactory().newQuery("CataloguePageLoader");
		m_pages = (Vector<CataloguePage>)Environment.getDatabase().query(query);
	}
	
	/**
	 * Clears the CatalogueArticle collection and loads the CatalogueArticle from the Database.
	 */
	@SuppressWarnings("unchecked")
	public void loadArticles()
	{
		DataQuery query = HabboHotel.getDataQueryFactory().newQuery("CatalogueArticleLoader");
		m_articles = (Vector<CatalogueArticle>)Environment.getDatabase().query(query);
	}
	
	public Item[] purchaseArticle(int articleID, String customData, int userID, int receiverID, boolean isGift, String giftNote)
	{
		CatalogueArticle article = this.getArticle(articleID);
		if (article != null)
		{
			// Create item
			Item item = HabboHotel.getItemAdmin().newItem();
			Item[] shipping = null;
			
			// For this user!
			item.ownerID = receiverID;
			
			// Determine actual item to ship (presentbox or article item)
			if (isGift)
			{
				// Create presentbox item
				item.definition = HabboHotel.getItemAdmin().getDefinitions().getPresentBoxDefinition();
				if (item.definition == null)
				{
					Log.error("Catalogue.purchaseArticle(): could not locate a present box item definition!");
					return null;
				}
				else
				{
					// Apply gift note
					item.customData = articleID + ":" + ((customData == null) ? "*" : customData) + "::" + giftNote;
				}
			}
			else
			{
				// Get articles item def
				item.definition = article.getItem();
				
				// Apply extra stuff
				if ((article.posterID > 0) && !item.definition.behaviour.isDecoration)
				{
					// Posters only!
					item.customData = Integer.toString(article.posterID);
				}
				else if (item.definition.behaviour.isPostIt)
				{
					item.customData = "20";
				}
				else if (customData != null)
				{
					item.customData = customData;
				}
			}
			
			// Store item
			if (HabboHotel.getItemAdmin().storeItem(item))
			{
				// Handle optional actions if not present box
				if (!isGift)
				{
					// Teleporter? (shipped in linking pairs)
					if (item.definition.behaviour.isTeleporter)
					{
						// Create linking teleporter
						Item item2 = HabboHotel.getItemAdmin().newItem();
						item2.definition = article.getItem();
						item2.ownerID = receiverID;
						item2.teleporterID = item.ID; // Link to item1
						
						// Store item2
						if (HabboHotel.getItemAdmin().storeItem(item2))
						{
							item.teleporterID = item2.ID; // Link to item2
							HabboHotel.getItemAdmin().updateItem(item); // Update item1 with new ID
							
							shipping = new Item[2];
							shipping[1] = item2;
						}
					}
				}
				
				// Add 'primary item' to shipping
				if (shipping == null)
				{
					shipping = new Item[1];
				}
				shipping[0] = item;
				
				// There it goes!
				return shipping;
			}
		}
		
		// Failed for whatever reason
		Log.error("Catalogue: purchase for article #" + articleID + " (gift: " + isGift + ") failed. Please consult error log.");
		return null;
	}
	
	public boolean shipClubGift(int userID, int month)
	{
		// Determine gift ID
		int giftID = month % 11; // 11 gifts - magic number
		
		// Determine gift article
		int articleID = HabboHotel.getPropBox().getInt("config.club.gift[" + giftID + "]", 0);
		if (articleID == 0)
		{
			Log.error("No config.club.gift[" + giftID + "] entry! Could not give club Gift!");
		}
		else
		{
			// Create note
			String note = HabboHotel.getPropBox().get("config.club.giftnote", "%giftid%").replace("%giftid%", Integer.toString(giftID));
			if (this.purchaseArticle(articleID, null, -1, userID, true, note) != null)
			{
				// Notify client if online
				CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(userID);
				if (client != null)
				{
					client.systemMsg(note);
				}
				
				// OK!
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Searches through the CataloguePage collection for a CataloguePage with a given ID.
	 * 
	 * @param pageID The ID of the CataloguePage to retrieve.
	 * @return The CataloguePage if the page is in the Catalogue, NULL otherwise.
	 */
	public CataloguePage getPage(int pageID)
	{
		for (CataloguePage page : m_pages)
		{
			if (page.ID == pageID)
				return page;
		}
		
		return null;
	}
	
	public CatalogueArticle getArticle(int articleID)
	{
		for (CatalogueArticle article : m_articles)
		{
			if (article.ID == articleID)
				return article;
		}
		
		return null;
	}
	
	public Vector<CatalogueArticle> getArticlesOnPage(int pageID)
	{
		Vector<CatalogueArticle> articles = new Vector<CatalogueArticle>();
		for (CatalogueArticle article : m_articles)
		{
			if (article.pageID == pageID)
				articles.add(article);
		}
		
		return articles;
	}
	
	/**
	 * Returns the total amount of pages in the Catalogue.
	 */
	public int pageAmount()
	{
		return m_pages.size();
	}
	
	/**
	 * Returns the total amount of articles in the Catalogue.
	 */
	public int articleAmount()
	{
		return m_articles.size();
	}
	
	/**
	 * Returns the CataloguePage collection representing the pages in the Catalogue.
	 */
	public Vector<CataloguePage> getPages()
	{
		return m_pages;
	}
}
