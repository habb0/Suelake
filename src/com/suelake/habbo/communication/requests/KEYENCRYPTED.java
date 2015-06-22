package com.suelake.habbo.communication.requests;

import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class KEYENCRYPTED implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		String keyEncrypted = msg.nextArgument();
		if (keyEncrypted.equals(comm.keyEncrypted))
		{
			// Key OK!
			comm.cryptoOK();
		}
		else
		{
			comm.stop("bad crypto auth challenge response");
		}
	}
}
