package com.suelake.habbo.communication.requests;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;

public class MESSENGER_MARKREAD implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		int messageID = Integer.parseInt(msg.nextArgument());
		HabboHotel.getMessengerService().markMessageAsRead(messageID);
	}
}
