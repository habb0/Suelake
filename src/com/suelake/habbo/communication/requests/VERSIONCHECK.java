package com.suelake.habbo.communication.requests;


import com.blunk.Log;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class VERSIONCHECK implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// Verify client version
		String clientVersion = msg.nextArgument();
		if(clientVersion.equals("client103"))
		{
			// Initialize encryption (all client data is deciphered from now on)
			comm.cryptoInit();
		}
		else
		{
			// Bad client!
			Log.info("Game client of " + comm.toString() + " had invalid client version '" + clientVersion + "'");
			comm.stop("invalid client version");
		}
	}
}
