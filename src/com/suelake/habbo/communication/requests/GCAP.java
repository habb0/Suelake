package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.catalogue.CataloguePage;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class GCAP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// What cp (CATALOGUEPAGE!11) does client request?
		String type = msg.nextArgument('/');
		int pageID = Integer.parseInt(msg.nextArgument('/'));
		String lang = msg.nextArgument('/');
		
		// Retrieve CataloguePage from Catalogue and perform some checks!
		CataloguePage page = HabboHotel.getCatalogue().getPage(pageID);
		if(page != null && comm.getUserObject().role >= page.accessRole)
		{
			comm.response.set("C_P");
			comm.response.appendObject(page);
			comm.sendResponse();
		}
	}
}
