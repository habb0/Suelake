package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class GETCREDITS implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.response.set("WALLETBALANCE");
		comm.response.appendArgument(Integer.toString(comm.getUserObject().credits));
		comm.sendResponse();
	}
}
