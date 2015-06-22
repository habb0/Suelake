package com.suelake.habbo.catalogue.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.catalogue.CatalogueArticle;
import com.suelake.habbo.catalogue.CatalogueArticleLoader;

public class SQLCatalogueArticleLoader extends CatalogueArticleLoader implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<CatalogueArticle> query(Connection conn) throws SQLException
	{
		// Execute query
		ResultSet result = conn.createStatement().executeQuery("SELECT * FROM catalogue_articles ORDER BY id;");
		
		// Fetch results
		Vector<CatalogueArticle> articles = new Vector<CatalogueArticle>();
		while(result.next())
		{
			CatalogueArticle article = new CatalogueArticle();
			if(article.setInnerItem(result.getInt("item_definitionid")))
			{
				// Parse article
				article.ID = result.getInt("id");
				article.pageID = result.getInt("pageid");
				article.price = result.getInt("price");
				article.posterID = result.getInt("item_posterid");
				
				// Parsed
				articles.add(article);
			}
		}
		
		result.close();
		
		// Return results
		return articles;
	}
}
