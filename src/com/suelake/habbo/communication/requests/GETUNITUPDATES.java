package com.suelake.habbo.communication.requests;

import java.text.NumberFormat;
import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientCommands;
import com.suelake.habbo.communication.ClientMessage;
import com.suelake.habbo.communication.ClientRequestHandler;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.spaces.Space;

public class GETUNITUPDATES implements ClientRequestHandler
{
	public void handle(ClientMessage msg, CommunicationHandler comm)
	{
		comm.getRequestHandlers().callHandler(ClientCommands.GETALLUNITS);
		if(true)
			return;
		
		/*
		 * if (tLine.item.count > 2) then 

      tSubOrderNum = 1
      repeat with j = 3 to tLine.item.count
        tSub = [:]
        tTempUnitID = string(tUnitPort) & "/" & tSubOrderNum

        if variableExists(tTempUnitID) then 
          tSubId = getVariable(tTempUnitID)
          tSub.setAt(#usercount, integer(tLine.item[j]))
          tList.setAt(tSubId, tSub)
          tSubOrderNum = (tSubOrderNum + 1)
        end if

      end repeat


    end repeat
		 */
		
		// Prepare number formatter (###)
		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setMinimumIntegerDigits(3);
		
		comm.response.set("UNITUPDATES");
		
		Vector<Space> spaces = HabboHotel.getSpaceAdmin().findPublicSpaces();
		for (Space space : spaces)
		{
			comm.response.appendNewArgument("unit" + numFormat.format(space.ID));
			comm.response.appendTabArgument("0");
			comm.response.appendTabArgument(Integer.toString(space.usersNow));
		}
		
		comm.sendResponse();
	}
}
