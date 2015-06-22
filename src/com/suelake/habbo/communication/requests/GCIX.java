package com.suelake.habbo.communication.requests;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.catalogue.CataloguePage;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;


public class GCIX implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// What Catalogue index does client request?
		String type = msg.nextArgument('/');
		String lang = msg.nextArgument('/');
		
		// Get the CataloguePages from the Catalogue
		Vector<CataloguePage> pages = HabboHotel.getCatalogue().getPages();
		
		// Serialize the pages into an index
		comm.response.set("C_I");
		for(CataloguePage page : pages)
		{
			// Visible for this User role?
			if(comm.getUserObject().role >= page.accessRole)
			{
				comm.response.appendNewArgument(Integer.toString(page.ID));
				comm.response.appendTabArgument(page.name);
			}
		}
		comm.sendResponse();
	}
}
