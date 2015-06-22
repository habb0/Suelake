package com.suelake.habbo.catalogue.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.catalogue.CataloguePage;
import com.suelake.habbo.catalogue.CataloguePageLoader;

public class SQLCataloguePageLoader extends CataloguePageLoader implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<CataloguePage> query(Connection conn) throws SQLException
	{
		// Execute query
		ResultSet result = conn.createStatement().executeQuery("SELECT * FROM catalogue_pages ORDER BY orderid;");
		
		// Fetch results
		Vector<CataloguePage> pages = new Vector<CataloguePage>();
		while(result.next())
		{
			CataloguePage page = new CataloguePage();
			page.ID = result.getInt("id");
			page.name = result.getString("name");
			page.accessRole = result.getByte("accessrole");
			page.layoutType = result.getString("layout");
			page.headlineImage = result.getString("img_headline");
			page.teaserImages = result.getString("img_teasers");
			page.body = result.getString("body");
			page.extraText = result.getString("extratext");
			
			pages.add(page);
		}
		
		result.close();
		
		// Return results
		return pages;
	}
	
}
