package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class SETSTUFFDATA implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		/**
		 * Note: bam figured out that you can inject a newline in customdata and create items that appear clientside for users in the flat.
		 * I (Nillus) have decided not to filter it. ;)
		 */
		
		int itemID = Integer.parseInt(msg.nextArgument('/'));
		String dataClass = msg.nextArgument('/');
		//String data = SecurityUtil.filterInput(msg.nextArgument('/'));
		String data = msg.nextArgument();
		
		comm.getSpaceInstance().getInteractor().updateActiveObjectData(comm.clientID, itemID, data);
	}
}
