package com.suelake.habbo.catalogue;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * CataloguePage represents a page in the Catalogue.\r
 * A CataloguePage advertises CatalogueArticles by displaying them with name, description and cost in Credits.
 * 
 * @author Nillus
 */
public class CataloguePage implements SerializableObject
{
	/**
	 * The database ID of this CataloguePage.
	 */
	public int ID;
	/**
	 * The name String of this CataloguePage.
	 */
	public String name;
	/**
	 * The minimum access role a User needs to have to gain access to this page.
	 */
	public byte accessRole;
	
	/**
	 * The body text on this page as a String.
	 */
	public String body;
	/**
	 * The layout 'type' of this page as a String. Layout types define the shape of the page, location of things etc.
	 */
	public String layoutType;
	/**
	 * The name of the 'headline image' to show above this page as a String.
	 */
	public String headlineImage;
	/**
	 * The name(s) of the 'teaser image(s)' to show in this page depending on the model. Multiple images can be separated by commas.
	 */
	public String teaserImages;
	public String extraText;
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendKV2Argument("i", Integer.toString(this.ID));
		msg.appendKV2Argument("n", this.name);
		msg.appendKV2Argument("l", this.layoutType);
		msg.appendKV2Argument("g", this.headlineImage);
		msg.appendKV2Argument("e", this.teaserImages);
		msg.appendKV2Argument("h", this.body);
		msg.appendKV2Argument("w", "Click on the article you want for more information");
		if (extraText != null)
		{
			msg.appendKV2Argument("s", extraText);
		}
		
		for (CatalogueArticle article : HabboHotel.getCatalogue().getArticlesOnPage(this.ID))
		{
			msg.appendObject(article);
		}
	}
}
