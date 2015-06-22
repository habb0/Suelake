package com.suelake.habbo.util;

import com.blunk.Log;
import com.suelake.habbo.communication.CommunicationHandler;

/**
 * ChatCommandParser is able to parse 'chat commands' (like for moderation etc) from user composed chat messages.
 * 
 * @author Nillus
 */
public class ChatCommandParser
{
	private final static char CHATCOMMAND_START = ':';
	
	public static boolean parseCommand(CommunicationHandler comm, String text)
	{
		try
		{
			if (text.charAt(0) == ChatCommandParser.CHATCOMMAND_START)
			{
				// Strip off leading character
				text = text.substring(1);
				
				// Determine command
				int indexOfWhiteSpace = text.indexOf(' ', 1);
				String command = (indexOfWhiteSpace == -1) ? text : text.substring(0, indexOfWhiteSpace);
				String body = (indexOfWhiteSpace == -1) ? null : text.substring(indexOfWhiteSpace + 1);
				
				// General commands
				if (command.equals("commands"))
				{
					return ChatCommandHandler.showCommandList(comm);
				}
				if (command.equals("about"))
				{
					return ChatCommandHandler.showAbout(comm);
				}
				else if (command.equals("status"))
				{
					return ChatCommandHandler.showStatus(comm);
				}
				else if(command.equals("stock") || command.equals("stocks"))
				{
					return ChatCommandHandler.handleStocksCommand(comm, body);
				}
				else if(command.equals("moonwalk"))
				{
					return ChatCommandHandler.walkReverse(comm);
				}
				else if(command.equals("ktd"))
				{
					return ChatCommandHandler.handleKickToDoor(comm, body);
				}
				else if(command.equals("mybots"))
				{
					return ChatCommandHandler.handleMyBots(comm, body);
				}
				else if(command.equals("position"))
				{
					return ChatCommandHandler.handlePositionRequest(comm);
				}
				else if (command.equals("servcast"))
				{
					return ChatCommandHandler.sendServCast(comm, body);
				}
				else if (command.equals("who"))
				{
					return ChatCommandHandler.showUserList(comm);
				}
				else if (command.equals("modcredits"))
				{
					return ChatCommandHandler.modCredits(comm, body);
				}
				else if (command.equals("kill"))
				{
					return ChatCommandHandler.killUser(comm, body);
				}
				else if (command.equals("invisible"))
				{
					return ChatCommandHandler.goInvisible(comm);
				}
				else if (command.equals("alert"))
				{
					return ChatCommandHandler.alertUser(comm, body);
				}
				else if (command.equals("kick"))
				{
					return ChatCommandHandler.kickUser(comm, body);
				}
				else if (command.equals("roomalert"))
				{
					return ChatCommandHandler.alertSpace(comm, body);
				}
				else if (command.equals("roomkick"))
				{
					return ChatCommandHandler.kickSpace(comm, body);
				}
				else if (command.equals("ban"))
				{
					return ChatCommandHandler.banUser(comm, body);
				}
				else if (command.equals("superban"))
				{
					return ChatCommandHandler.banUserIp(comm, body);
				}
				else if (command.equals("unban"))
				{
					return ChatCommandHandler.unbanUser(comm, body);
				}
				else if(command.equals("unbanip"))
				{
					return ChatCommandHandler.unbanIp(comm, body);
				}
				else if(command.equals("reloadcata"))
				{
					return ChatCommandHandler.reloadCatalogue(comm);
				}
				else if(command.equals("bots"))
				{
					return ChatCommandHandler.botsHandler(comm, body);
				}
				else if(command.equals("transfer"))
				{
					return ChatCommandHandler.handleTransfer(comm, body);
				}
				else if(command.equals("server"))
				{
					return ChatCommandHandler.handleServerFunctions(comm, body);
				}
				else if(command.equals("eval"))
				{
					return ChatCommandHandler.handleShellEvaluation(comm, body);
				}
				else if(command.equals("call"))
				{
					return ChatCommandHandler.handleShellCall(comm, body);
				}
			}
		}
		catch (Exception ex)
		{
			Log.error("ChatCommandParser: error while parsing/handling chat input \"" + text + "\"", ex);
			comm.systemMsg("ERROR!\rYour chat input \"" + text + "\" was parsed in the ChatCommandParser but caused an error serverside.\rThis often indicates bad parameters/syntax.\rThe error message was: " + ex.getMessage());
			return true;
		}
		
		// Non-recognized command!
		return false;
	}
}
