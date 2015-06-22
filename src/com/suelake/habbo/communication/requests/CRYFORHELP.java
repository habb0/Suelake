package com.suelake.habbo.communication.requests;


import com.blunk.util.KeyValueStringReader;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.moderation.CallForHelp;
import com.suelake.habbo.util.SecurityUtil;

public class CRYFORHELP implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		// In space?
		if (comm.getSpaceInstance() == null)
		{
			comm.systemMsg("Sorry, but we can't help you at your current position.");
		}
		else
		{
			// Get data from the 'cry for help', such as space name, message etc
			KeyValueStringReader callData = new KeyValueStringReader(msg.getBody(), ":");
			
			// Construct CallForHelp
			CallForHelp myCall = HabboHotel.getModerationCenter().createCallForHelp();
			myCall.setSender(comm.getUserObject());
			myCall.setSpace((comm.getSpaceInstance() != null) ? comm.getSpaceInstance().getInfo() : null);
			myCall.text = SecurityUtil.filterInput(callData.read("text"));
			
			// Submit call
			HabboHotel.getModerationCenter().submitCallForHelp(myCall);
		}
	}
}
