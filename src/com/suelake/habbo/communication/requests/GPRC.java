package com.suelake.habbo.communication.requests;

import com.blunk.Environment;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.catalogue.CatalogueArticle;
import com.suelake.habbo.catalogue.CataloguePage;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;
import com.suelake.habbo.users.User;

public class GPRC implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String[] purchaseData = msg.getBody().split("\r");
		
		// Article
		int pageID = Integer.parseInt(purchaseData[2]);
		int articleID = Integer.parseInt(purchaseData[4]);
		
		// Shipping
		int receiverID = comm.getUserObject().ID;
		
		// Custom data to article's items
		String customData = null;
		
		// Gift data
		boolean asGift = (purchaseData[6].equals("1"));
		String giftNote = null;
		
		// Get page and article
		CataloguePage page = HabboHotel.getCatalogue().getPage(pageID);
		CatalogueArticle article = HabboHotel.getCatalogue().getArticle(articleID);
		
		// Page accessible?
		if (page == null || page.accessRole > comm.getUserObject().role)
		{
			comm.response.set("SYSTEMBROADCAST"); // PURCHASE_ERROR
			comm.response.appendArgument("Purchase error: page not found.");
		}
		else
		{
			// Article accessible?
			if (article == null || article.pageID != page.ID)
			{
				comm.response.set("SYSTEMBROADCAST"); // PURCHASE_ERROR
				comm.response.appendArgument("Purchase error: product not found.");
			}
			else
			{
				// Enough balance to buy article?
				if (article.price > comm.getUserObject().credits)
				{
					comm.response.set("PURCHASE_NOBALANCE");
				}
				else
				{
					// Check gift?
					if (asGift)
					{
						User receiver = HabboHotel.getUserRegister().getUserInfo(purchaseData[7], false);
						if (receiver == null)
						{
							comm.systemMsg("Sorry, but the user '" + purchaseData[7] + "' does not exist.\rPurchasing of product cancelled.");
							return;
						}
						else
						{
							// Correct case sensivity for 'purchase ok' message
							purchaseData[7] = receiver.name; 
							
							// Set note & receiverID
							receiverID = receiver.ID;
							giftNote = (purchaseData.length >= 9) ? purchaseData[8] : "";
						}
					}
					
					// Process custom data
					if (article.getItem().behaviour.isDecoration)
					{
						try
						{
							customData = Integer.toString(Integer.parseInt(purchaseData[5]));
						}
						catch (NumberFormatException ex)
						{
							// Such a bad hax...
							return;
						}
					}
					
					// Order shipping
					Item[] shipping = HabboHotel.getCatalogue().purchaseArticle(articleID, customData, comm.getUserObject().ID, receiverID, asGift, giftNote);
					
					// Item delivered?
					if (shipping == null)
					{
						comm.response.set("SYSTEMBROADCAST");
						comm.response.appendArgument("Sorry, but currently we are unable to ship that product.\rPlease try again later.");
					}
					else
					{
						// Set the result message
						if(!asGift)
						{
							comm.response.set("PURCHASE_OK");
						}
						else
						{
							comm.response.set("SYSTEMBROADCAST");
							comm.response.appendArgument("Successfully purchased, giftwrapped and shipped the product to " + purchaseData[7] + "!");
						}
						
						// Pay credits
						comm.getUserObject().credits -= article.price;
						comm.sendCredits();
						HabboHotel.getUserRegister().updateUser(comm.getUserObject());
						
						// Add item(s) to receiver's inventory
						CommunicationHandler receiver = HabboHotel.getGameClients().getClientOfUser(receiverID);
						if (receiver != null)
						{
							// Add item(s) to inventory
							for (Item item : shipping)
							{
								receiver.getItemInventory().addItem(item);
							}
							
							// Same user than purchasing user?
							if (asGift)
							{
								receiver.getItemInventory().sendStrip("last");
								
								final String[] pokes = { "Abra", "Mudkip", "Pikachu", "Metapod", "Bulbasaur", "Weedle", "Rhyhorn", "Pidgey" };
								receiver.systemMsg("What? " + pokes[Environment.getRandom().nextInt(pokes.length)] + " is evolvin... - umm, you have received a gift from someone!");
							}
						}
					}
				}
			}
		}
		
		// Send purchase result
		comm.sendResponse();
	}
}
