package com.suelake.habbo.communication;

public interface ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm);
}
