package com.suelake.habbo.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blunk.Environment;
import com.blunk.shell.BlunkShell;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.ClientCommands;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.economy.Company;
import com.suelake.habbo.economy.Stock;
import com.suelake.habbo.economy.VirtualEconomy;
import com.suelake.habbo.moderation.ModerationBan;
import com.suelake.habbo.net.InfoConnection;
import com.suelake.habbo.spaces.instances.SpaceBot;
import com.suelake.habbo.spaces.instances.SpaceBotMode;
import com.suelake.habbo.spaces.instances.SpaceUser;
import com.suelake.habbo.spaces.pathfinding.RotationCalculator;
import com.suelake.habbo.users.User;

public class ChatCommandHandler
{
	private final static String CHATCOMMAND_ABOUT_TXT = "¶&¶	Project Blunk" + "\r" + "¶&¶	Habbo Hotel V5 server emulator" + "\r" + "¶&¶	Platform: Java" + "\r" + "¶&¶	Authors: Nils [nillus] and Mike [office.boy]" + "\r" + "¶&¶	Special thanks go to:"
			+ "\r" + "¶&¶		- Matthew Parlane" + "\r" + "¶&¶		- Joe 'Joeh' Hegarty" + "\r" + "¶&¶		- Aapo 'kyrpov' Kyrola";
	private final static int CHATCOMMAND_WHO_USERSPERPAGE = 20;
	
	public static boolean showAbout(CommunicationHandler comm)
	{
		comm.systemMsg(ChatCommandHandler.CHATCOMMAND_ABOUT_TXT);
		return true;
	}
	
	public static boolean showStatus(CommunicationHandler comm)
	{
		// Retrieve the statistics
		int maxConns = HabboHotel.getGameClients().max();
		int liveConns = HabboHotel.getGameClients().count();
		long uptime = (TimeHelper.getTime() - HabboHotel.getStartTime());
		long days = (uptime / (1000 * 60 * 60 * 24));
		long hours = (uptime - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (uptime - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (uptime - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60) - minutes * (1000 * 60)) / (1000);
		Runtime runtime = Runtime.getRuntime();
		
		// Determine client type
		String clientType = comm.getConnection().isMacintosh() ? "Mac OS X/" + InfoConnection.DATA_ENCODING_MAC : "Windows/" + InfoConnection.DATA_ENCODING_WIN;
		
		// Build the window message
		ServerMessage wnd = new ServerMessage("SYSTEMBROADCAST");
		wnd.appendNewArgument("SERVER");
		wnd.appendNewArgument("Server uptime is " + days + " day(s), " + hours + " hour(s), " + minutes + " minute(s) and " + seconds + " second(s).");
		wnd.appendNewArgument("Currently there are " + liveConns + "/" + maxConns + " connections in use.");
		wnd.appendNewArgument("Your client ID is " + comm.clientID + " and your client type is " + clientType + ".");
		wnd.appendNewArgument("");
		wnd.appendNewArgument("SYSTEM");
		wnd.appendNewArgument("CPU cores: " + runtime.availableProcessors());
		wnd.appendNewArgument("JVM memory usage: " + (runtime.totalMemory() / 1024) + " KB");
		comm.sendMessage(wnd);
		
		return true;
	}
	
	public static boolean showCommandList(CommunicationHandler comm)
	{
		ServerMessage list = new ServerMessage("SYSTEMBROADCAST");
		list.appendArgument("You can use the following chat commands:\r");
		
		if (comm.getUserObject().hasRight("can_see_server_info"))
		{
			list.appendNewArgument(":about\tDisplays server information");
		}
		if (comm.getUserObject().hasRight("can_see_server_status"))
		{
			list.appendNewArgument(":status\tDisplays server status");
		}
		if (comm.getUserObject().hasRight("can_access_stocks"))
		{
			list.appendNewArgument(":stocks help\tDisplays information on the Stocks Market");
		}
		if (comm.getUserObject().hasRight("can_alert_user"))
		{
			list.appendNewArgument(":alert %user% %msg%\tAlerts %user% with message %msg%");
		}
		if (comm.getUserObject().hasRight("can_kick_user"))
		{
			list.appendNewArgument(":kick %user% [%msg%]\tKicks %user% from space displaying optional message %msg%");
		}
		if (comm.getUserObject().hasRight("can_alert_space"))
		{
			list.appendNewArgument(":roomalert %msg%\tAlerts all users in your current space with %msg%");
		}
		if (comm.getUserObject().hasRight("can_kick_space"))
		{
			list.appendNewArgument(":roomkick %msg%\tKicks all users below your role in your current space, alerting them with %msg%");
		}
		if (comm.getUserObject().hasRight("can_ban_user"))
		{
			list.appendNewArgument(":ban %user% %hours% %reason%\tApplies a moderation ban to %user% for %hours% with %reason%");
		}
		if (comm.getUserObject().hasRight("can_ban_ip"))
		{
			list.appendNewArgument(":superban %user% %hours% %reason%\tApplies a moderation ban to %user% and %user%'s IP address for %hours% with %reason%");
			list.appendNewArgument(":banip %ip% %hours% %reason%\tApplies a moderation ban to %ip% for %hours% with %reason%");
		}
		if (comm.getUserObject().hasRight("can_unban"))
		{
			list.appendNewArgument(":unban %user%\tRemoves all moderation bans (+ IP bans) for %user%");
			list.appendNewArgument(":unbanip %ip%\tRemoves all moderation bans for IP address %ip%");
		}
		if (comm.getUserObject().hasRight("can_be_invisible"))
		{
			list.appendNewArgument(":invisible\tMakes your avatar invisible in the space");
		}
		if (comm.getUserObject().hasRight("can_moonwalk"))
		{
			list.appendNewArgument(":moonwalk\tEnables/disables your avatar's moonwalk status (reverse walking)");
		}
		if (comm.getUserObject().hasRight("can_kick_to_door"))
		{
			list.appendNewArgument(":ktd x\tKicks x to door. Requires rights!");
		}
		if (comm.getUserObject().hasRight("can_kill_user"))
		{
			list.appendNewArgument(":kill %user%\tDisconnects %user% from the server");
		}
		if (comm.getUserObject().hasRight("can_send_servcast"))
		{
			list.appendNewArgument(":servcast %msg%\tSends %msg% to all online users on the server");
		}
		if (comm.getUserObject().hasRight("can_see_userlist"))
		{
			list.appendNewArgument(":who\tDisplays a list with online users and their IP address");
		}
		if (comm.getUserObject().hasRight("can_modify_credits"))
		{
			list.appendNewArgument(":modcredits %user% %x%\tModifies the credit amount of %user% with %x%, %x% can be a positive or negative integer");
		}
		if (comm.getUserObject().hasRight("can_control_server"))
		{
			list.appendNewArgument(":server %function\tProvides various server functions, such as reloading configuration etc");
		}
		
		comm.sendMessage(list);
		return true;
	}
	
	public static boolean sendServCast(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_send_servcast"))
		{
			if (body != null)
			{
				ServerMessage msg = new ServerMessage("SYSTEMBROADCAST");
				msg.appendArgument("Message from Hotel staff:");
				msg.appendNewArgument(body);
				
				for (CommunicationHandler client : HabboHotel.getGameClients().getClients())
				{
					client.sendMessage(msg);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean showUserList(CommunicationHandler comm)
	{
		if (comm.getUserObject().hasRight("can_see_userlist"))
		{
			// Get all logged in clients
			Vector<CommunicationHandler> clients = HabboHotel.getGameClients().getLoggedInClients();
			
			// Prepare message
			comm.response.set("SYSTEMBROADCAST");
			comm.response.appendArgument("There are " + clients.size() + " user(s) logged in.\r");
			
			int counter = 0;
			for (CommunicationHandler client : clients)
			{
				// Past page?
				if ((++counter % ChatCommandHandler.CHATCOMMAND_WHO_USERSPERPAGE) == 0)
				{
					// New page
					comm.sendResponse();
					comm.response.set("SYSTEMBROADCAST");
					comm.response.appendArgument("There are " + clients.size() + " user(s) logged in.\r");
				}
				
				// "1.	Administrator	127.0.0.1"
				// "2.	Administrator2	127.0.0.1"
				comm.response.appendNewArgument(counter + ".");
				comm.response.appendTabArgument(client.getUserObject().name);
				comm.response.appendTabArgument(client.getConnection().getIpAddress());
			}
			
			// Send response
			comm.sendResponse();
			
			return true;
		}
		
		return false;
	}
	
	public static boolean modCredits(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_modify_credits"))
		{
			String[] args = body.split(" ", 2);
			if (args.length == 2)
			{
				String name = args[0];
				short amount = Short.parseShort(args[1]);
				
				User usr = HabboHotel.getUserRegister().getUserInfo(name, true);
				if (usr == null)
				{
					comm.systemMsg("User \"" + name + "\" was not found.");
				}
				else
				{
					usr.credits += amount;
					HabboHotel.getUserRegister().updateUser(usr);
					
					// Update online?
					CommunicationHandler usrClient = HabboHotel.getGameClients().getClientOfUser(usr.ID);
					if (usrClient != null)
					{
						usrClient.sendCredits();
					}
					
					comm.systemMsg("Gave user " + usr.name + " " + amount + " credits, user now has " + usr.credits + ".");
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean killUser(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_kill_user"))
		{
			if (body != null)
			{
				CommunicationHandler target = HabboHotel.getGameClients().getClientOfUser(body);
				if (target != null)
				{
					target.stop("killed by " + comm.getUserObject().name);
					comm.systemMsg(target.getUserObject().name + " killed!");
				}
				else
				{
					comm.systemMsg(body + " not online!");
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean alertUser(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_alert_user"))
		{
			String[] args = body.split(" ", 2);
			if (args.length == 2)
			{
				CommunicationHandler target = HabboHotel.getGameClients().getClientOfUser(args[0]);
				if (target == null)
				{
					comm.systemMsg("Could not alert " + args[0] + ", user was not online.");
				}
				else
				{
					target.moderatorWarning(args[1]);//"[Personal Moderation Alert by " + comm.getUserObject().name + "]\r" + args[1]);
					comm.systemMsg("Alerted " + target.getUserObject().name + ".");
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean kickUser(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_kick_user"))
		{
			String[] args = body.split(" ", 2);
			if (args.length >= 1)
			{
				CommunicationHandler target = HabboHotel.getGameClients().getClientOfUser(args[0]);
				if (target == null || target.getUserObject().role > comm.getUserObject().role)
				{
					comm.systemMsg("Could not alert " + args[0] + ", user was not online, or you have bad permissions.");
				}
				else
				{
					// Reason given?
					if (args.length == 2)
					{
						target.kickFromSpace(args[1]);
					}
					else
					{
						target.kickFromSpace(null);
					}
					comm.systemMsg("Kicked " + target.getUserObject().name + " from space.");
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean alertSpace(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_alert_space"))
		{
			if (body == null)
			{
				comm.systemMsg("Could not alert space, please supply a message.");
			}
			else
			{
				ServerMessage msg = new ServerMessage("ERROR");
				msg.appendArgument("MODERATOR WARNING");
				msg.appendArgument(body, '/');
				comm.getSpaceInstance().broadcast(msg);
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean kickSpace(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_kick_space"))
		{
			if (body == null)
			{
				comm.systemMsg("Could not kick space, please supply a message.");
			}
			else
			{
				comm.getSpaceInstance().moderationKick(comm.getUserObject().role, body);
				
				comm.systemMsg("All users below your role have been kicked from the space.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean goInvisible(CommunicationHandler comm)
	{
		if (comm.getUserObject().hasRight("can_be_invisible"))
		{
			// Get space user
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if (usr.isInvisible)
			{
				// Broadcast 'entry' to already inside clients for re-appear
				ServerMessage msg = new ServerMessage("USERS");
				msg.appendObject(usr);
				comm.getSpaceInstance().broadcast(msg);
			}
			else
			{
				// Broadcast 'remove from room' for already inside clients
				ServerMessage msg = new ServerMessage("LOGOUT");
				msg.appendArgument(usr.getUserObject().name);
				comm.getSpaceInstance().broadcast(msg);
			}
			
			// Flip flag
			usr.isInvisible = !usr.isInvisible;
			
			// Block/unblock tile
			comm.getSpaceInstance().getInteractor().setUserMapTile(usr.X, usr.Y, !usr.isInvisible);
			
			// Notify user
			comm.systemMsg("Invisible: " + Boolean.toString(usr.isInvisible));
			return true;
		}
		
		return false;
	}
	
	public static boolean handlePositionRequest(CommunicationHandler comm)
	{
		SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
		if (usr != null)
		{
			comm.systemMsg("Your current position in the room is:\rX=" + usr.X + "\rY=" + usr.Y);
		}
		return true;
	}
	
	public static boolean reloadCatalogue(CommunicationHandler comm)
	{
		if (comm.getUserObject().hasRight("can_alert_user"))
		{
			HabboHotel.getCatalogue().loadPages();
			HabboHotel.getCatalogue().loadArticles();
			
			sendServCast(comm, "Catalogue Reloaded!");
			
			return true;
		}
		
		return false;
	}
	
	public static boolean banUser(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_ban_user"))
		{
			ChatCommandHandler.internalBanUser(comm, body, false);
			return true;
		}
		
		return false;
	}
	
	public static boolean banUserIp(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_ban_user") && comm.getUserObject().hasRight("can_ban_ip"))
		{
			ChatCommandHandler.internalBanUser(comm, body, true);
			return true;
		}
		
		return false;
	}
	
	private static void internalBanUser(CommunicationHandler comm, String body, boolean banIP)
	{
		String[] args = body.split(" ", 3);
		if (args.length == 3)
		{
			// Parse the arguments
			String username = args[0];
			int hours = Integer.parseInt(args[1]);
			String reason = args[2];
			
			// Get user
			User usr = HabboHotel.getUserRegister().getUserInfo(username, true);
			if (usr == null)
			{
				comm.systemMsg("Ban failed. User '" + username + "' does not exist.");
			}
			else if (usr.role > comm.getUserObject().role)
			{
				comm.systemMsg("Ban failed. You do not have enough permissions to ban " + usr.name);
			}
			else
			{
				// Apply the ban
				ModerationBan ban = HabboHotel.getModerationCenter().setUserBan(usr.ID, banIP, hours, reason, comm.getUserObject().ID);
				if (ban == null)
				{
					comm.systemMsg("Ban failed. Please verify if you have given valid details and you have the required privileges.");
				}
				else
				{
					comm.systemMsg(ban.generateReport());
				}
			}
		}
		else
		{
			comm.systemMsg("Ban failed. Please supply username, hours and ban reason.");
		}
	}
	
	public static boolean unbanUser(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_unban"))
		{
			if (body != "")
			{
				// Get user
				User usr = HabboHotel.getUserRegister().getUserInfo(body, false);
				if (usr == null)
				{
					comm.systemMsg("Unban failed. User '" + body + "' does not exist.");
				}
				else
				{
					ModerationBan ban = HabboHotel.getModerationCenter().getUserBan(usr.ID);
					if (ban == null)
					{
						comm.systemMsg("Unban failed. No bans for " + usr.name + ".");
					}
					else
					{
						if (HabboHotel.getModerationCenter().deleteBan(ban))
						{
							comm.systemMsg("Unban OK. The following ban was deleted:\r" + ban.generateReport());
						}
						else
						{
							comm.systemMsg("Unban failed. Ban was found, but system was unable to delete it for whatever reason.\r" + ban.generateReport());
						}
					}
				}
			}
			else
			{
				comm.systemMsg("Unban failed. Please supply username.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean unbanIp(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_unban"))
		{
			if (body.length() > 0)
			{
				ModerationBan ban = HabboHotel.getModerationCenter().getIpBan(body);
				if (ban == null)
				{
					comm.systemMsg("Unban failed. No IP bans for " + body);
				}
				else
				{
					if (HabboHotel.getModerationCenter().deleteBan(ban))
					{
						comm.systemMsg("Unban OK. The following ban was deleted:\r" + ban.generateReport());
					}
					else
					{
						comm.systemMsg("Unban failed. Ban was found, but system was unable to delete it for whatever reason.\r" + ban.generateReport());
					}
				}
			}
			else
			{
				comm.systemMsg("Unban failed. Please supply IP address.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean handleServerFunctions(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_control_server"))
		{
			if (body != null)
			{
				String arg[] = body.split(" ");
				if (arg[0].equals("setmotd"))
				{
					// Determine the new MOTD
					arg = body.split(" ", 2);
					if (arg.length == 2)
					{
						// Blank?
						if (arg[1].equals("null"))
						{
							arg[1] = null;
						}
						
						// Set it
						HabboHotel.getAccessControl().setMessageOfTheDay(arg[1]);
						
						// Broadcast it to existing clients
						if (arg[1] != null)
						{
							ChatCommandHandler.sendServCast(comm, arg[1]);
						}
						
						// Done
						comm.systemMsg("New MOTD successfully set.");
					}
					else
					{
						comm.systemMsg("Please specify a new Message Of The Day, or 'null' to disable the message.");
					}
				}
				else if (arg[0].equals("killemptyconnections"))
				{
					int amount = 0;
					comm.systemMsg("Server killed " + amount + " empty connections.");
				}
				else if (arg[0].equals("reloaditemdefinitions"))
				{
					int amount = HabboHotel.getItemAdmin().getDefinitions().loadDefinitions();
					comm.systemMsg("ItemDefinitions reloaded, server knows " + amount + " definitions.");
				}
				else if (arg[0].equals("reloadcatalogue"))
				{
					// Re-load from Database
					HabboHotel.getCatalogue().loadArticles();
					HabboHotel.getCatalogue().loadPages();
					
					// Notification
					comm.systemMsg("Catalogue reloaded, " + HabboHotel.getCatalogue().pageAmount() + " pages, " + HabboHotel.getCatalogue().articleAmount() + " articles.");
				}
				else if (arg[0].equals("reloadspacemodels"))
				{
					// Re-load from Database
					HabboHotel.getSpaceAdmin().getModels().loadModels();
					comm.systemMsg("Reloaded SpaceModels.");
				}
				else if (arg[0].equals("reloaduserrights"))
				{
					HabboHotel.getAccessControl().loadUserRights();
					comm.systemMsg("Reloaded user rights.");
				}
				else if (arg[0].equals("clearcfhs"))
				{
					int amount = HabboHotel.getModerationCenter().clearPendingCalls();
					comm.systemMsg("Cleared " + amount + " pending CFHs.");
				}
				else
				{
					// Send a window with all of the available :server parameters
					ServerMessage wnd = new ServerMessage("SYSTEMBROADCAST");
					wnd.appendNewArgument("Invalid argument for server functions, following functions are valid:");
					wnd.appendNewArgument("setmotd\tSets the Message Of The Day, shown to Users at login");
					wnd.appendNewArgument("killemptyconnections\tKills all connections with no UserObject");
					wnd.appendNewArgument("reloaditemdefinitions\tReloads the ItemDefinitions");
					wnd.appendNewArgument("reloadcatalogue\tReloads the Catalogue pages and articles");
					wnd.appendNewArgument("reloadspacemodels\tReloads the SpaceModels from the Database");
					wnd.appendNewArgument("reloaduserrights\tReloads the UserRightContainers");
					wnd.appendNewArgument("clearcfhs\tDrops the pending Call for Helps");
					comm.sendMessage(wnd);
				}
			}
			else
			{
				comm.systemMsg("Please specify a server function, or type :server help for help.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean walkReverse(CommunicationHandler comm)
	{
		if (comm.getUserObject().hasRight("can_moonwalk"))
		{
			// Swap flag
			SpaceUser usr = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			usr.isReverseWalk = !usr.isReverseWalk;
			
			// Yay!
			comm.systemMsg("Billie Jean status: " + Boolean.toString(usr.isReverseWalk));
			return true;
		}
		
		return false;
	}
	
	public static boolean handleKickToDoor(CommunicationHandler comm, String body)
	{
		if (body != null && comm.getUserObject().hasRight("can_kick_to_door"))
		{
			SpaceUser me = comm.getSpaceInstance().getUserByClientID(comm.clientID);
			if(me.isFlatOwner)
			{
				SpaceUser usr = comm.getSpaceInstance().getUserByName(body);
				if (usr != null && usr.getUserObject().role <= me.getUserObject().role)
				{
					// Unblock old position
					comm.getSpaceInstance().getInteractor().setUserMapTile(usr.X, usr.Y, false);
					
					// Move to new position & mod tile
					usr.X = comm.getSpaceInstance().getModel().doorX;
					usr.Y = comm.getSpaceInstance().getModel().doorY;
					usr.Z = comm.getSpaceInstance().getModel().doorZ;
					comm.getSpaceInstance().getInteractor().setUserMapTile(usr.X, usr.Y, true);
					
					// Remove interactive statuses & refresh!
					usr.removeInteractiveStatuses();
					usr.ensureUpdate(true);
					return true;
				}
			}
		}
		
		// No permissions or user not found
		return false;
	}
	
	public static boolean handleStocksCommand(CommunicationHandler comm, String body)
	{
		if (!comm.getUserObject().hasRight("can_access_stocks"))
			return false;
		if (body == null)
		{
			body = "help";
		}
		
		String[] parts = body.split(" ");
		
		String action = parts[0];
		if (action.equals("invest"))
		{
			String companyID = parts[1];
			Short credits = Short.parseShort(parts[2]);
			
			// Valid stock?
			if (comm.getUserObject().credits < credits)
			{
				comm.systemMsg("Sorry, but you only have " + comm.getUserObject().credits + "!");
			}
			else if (credits < VirtualEconomy.STOCK_MIN)
			{
				comm.systemMsg("Sorry, but the minimum investment in a stock is " + VirtualEconomy.STOCK_MIN + " credits.\rTry to cough up some more credits!");
			}
			else if (credits > VirtualEconomy.STOCK_MAX)
			{
				comm.systemMsg("Sorry, but the maximum investment in a stock is " + VirtualEconomy.STOCK_MAX + " credits.\rTry spending that cash somewhere else!");
			}
			else
			{
				// Cannot have two stocks in same company at same time
				Vector<Stock> myStocks = HabboHotel.getEconomy().getStocksForUser(comm.getUserObject().ID);
				for (Stock stock : myStocks)
				{
					if (stock.companyID.equals(companyID))
					{
						comm.systemMsg("Sorry, but you already have a stock in " + stock.companyID + ", this stock has the ID " + stock.ID + ".\rYou have to wait atleast " + stock.getTimeLeft()
								+ " and redeem it before you can do another investment in this company.");
						return true;
					}
				}
				
				// Company exists?
				Company company = HabboHotel.getEconomy().getCompany(companyID);
				if (company == null)
				{
					comm.systemMsg("Sorry, company with ID \"" + companyID + "\" does not exist.");
				}
				else
				{
					Stock stock = HabboHotel.getEconomy().makeInvestment(comm.getUserObject().ID, company, credits);
					if (stock == null)
					{
						comm.systemMsg("Sorry, but your investment in " + company + " failed because of a technical problem.\rPlease try again later.");
					}
					else
					{
						// Remove credits from wallet and refresh
						comm.getUserObject().credits -= credits;
						comm.getRequestHandlers().callHandler(ClientCommands.GETCREDITS);
						HabboHotel.getUserRegister().updateUser(comm.getUserObject());
						
						// Yay!
						comm.systemMsg("Congratulations!\rYou have successfully invested " + credits + " credits in stock " + company.ID + "!\rThe scale of " + company.displayName + " is now " + company.scale + ".\rYour stock ID is " + stock.ID
								+ ".\rYou can redeem your stock in " + stock.getTimeLeft() + ".");
					}
				}
			}
		}
		else if (action.equals("redeem"))
		{
			int stockID = Integer.parseInt(parts[1]);
			
			// Get stock
			Stock stock = HabboHotel.getEconomy().getStock(stockID);
			
			// User did this stock?
			if (stock == null || stock.userID != comm.getUserObject().ID)
			{
				comm.systemMsg("Sorry, but you are not eligible to redeem this stock.");
			}
			else
			{
				if (!stock.isRedeemable())
				{
					comm.systemMsg("Sorry, but you cannot redeem this stock at the moment.\rThis stock can be redeemed in " + stock.getTimeLeft() + ".");
				}
				else
				{
					// Redeem the stock
					short result = HabboHotel.getEconomy().redeemStock(stock);
					
					// Give credits back (or whatever is left of it)
					comm.getUserObject().credits += result;
					comm.getRequestHandlers().callHandler(ClientCommands.GETCREDITS);
					HabboHotel.getUserRegister().updateUser(comm.getUserObject());
					
					// Delete the stock
					Environment.getDatabase().delete(stock);
					
					// Show the result message
					int diff = Math.abs(result - stock.credits);
					
					Company company = HabboHotel.getEconomy().getCompany(stock.companyID);
					comm.systemMsg("Stock " + stock.ID + " was an investment of " + stock.credits + " credits in " + company.ID + ", and was made at " + TimeHelper.formatDateTime(stock.moment) + ".\r" + company.displayName
							+ " has used the investment for their business, and you get " + result + " credits back.\r\r" + "This means that you have " + ((result > stock.credits) ? "gained" : "lost") + " " + diff + " credits.\r\r" + "The scale of "
							+ company.ID + " is currently " + company.scale + " and the risk factor is " + company.risk + ".\r" + "Thanks for investing in " + company + "!");
				}
			}
		}
		else if (action.equals("my"))
		{
			ServerMessage msg = new ServerMessage("SYSTEMBROADCAST");
			msg.appendArgument("You currently have the following stocks open. Type :stocks redeem <stockID> to redeem a stock.");
			
			// Append all open stocks
			Vector<Stock> mystocks = HabboHotel.getEconomy().getStocksForUser(comm.getUserObject().ID);
			for (Stock stock : mystocks)
			{
				msg.appendNewArgument("ID = " + stock.ID + ". Investment: " + stock.credits + " credits in " + stock.companyID);
				if (stock.isRedeemable())
				{
					msg.append("Redeemable.");
				}
				else
				{
					msg.append("Time till redeemable: " + stock.getTimeLeft() + ".");
				}
			}
			
			// Send the window
			comm.sendMessage(msg);
		}
		else if (action.equals("company") && parts.length == 2)
		{
			String companyID = parts[1];
			Company company = HabboHotel.getEconomy().getCompany(companyID);
			if (company == null)
			{
				comm.systemMsg("Company \"" + companyID + "\" does not exist.");
			}
			else
			{
				comm.systemMsg("Company ID: " + company.ID + "\rName: " + company.displayName + "\rScale: " + company.scale + "\rRisk: " + company.risk + "\rFunds: " + company.funds);
			}
		}
		else
		{
			comm.systemMsg("Syntax for this command is:\r" + ":stocks invest %companyID% %amount% - Invests %amount% credits in %companyID%\r" + ":stocks redeem %stockID% - Redeems the stock with ID=%stockID%\r"
					+ ":stocks my - Shows a list with your stocks and their state\r" + ":stocks company %companyID% - Shows information about company %companyID\r\r" + "Tip: search for Stocks for the official Stocks Market room.");
		}
		
		// Input always valid!
		return true;
	}
	
	public static boolean botsHandler(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_control_bots"))
		{
			if (body != null)
			{
				String[] arg = body.split(" ");
				if (arg[0].equals("spawn"))
				{
					if (arg.length == 3)
					{
						int amount = Integer.parseInt(arg[1]);
						if (amount > 100)
						{
							amount = 100;
						}
						
						final String[] NAMES_MALE = { "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven", "Edward", "Brian",
								"Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick",
								"Peter", "Harold", "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger", "Joe", "Juan", "Jack", "Albert", "Jonathan", "Justin", "Terry", "Gerald", "Keith", "Samuel", "Willie", "Ralph", "Lawrence", "Nicholas", "Roy",
								"Benjamin", "Bruce", "Brandon", "Adam", "Harry", "Fred", "Wayne", "Billy", "Steve", "Louis", "Jeremy", "Aaron", "Randy", "Howard", "Eugene", "Carlos", "Russell", "Bobby", "Victor", "Martin", "Ernest", "Phillip", "Todd",
								"Jesse", "Craig", "Alan", "Shawn", "Clarence", "Sean", "Philip", "Chris", "Johnny", "Earl", "Jimmy", "Antonio", "Danny", "Bryan", "Tony", "Luis", "Mike", "Stanley", "Leonard", "Nathan", "Dale", "Manuel", "Rodney",
								"Curtis", "Norman", "Allen", "Marvin", "Vincent", "Glenn", "Jeffery", "Travis", "Jeff", "Chad", "Jacob", "Lee", "Melvin", "Alfred", "Kyle", "Francis", "Bradley", "Jesus", "Herbert", "Frederick", "Ray", "Joel", "Edwin",
								"Don", "Eddie", "Ricky", "Troy", "Randall", "Barry", "Alexander", "Bernard", "Mario", "Leroy", "Francisco", "Marcus", "Micheal", "Theodore", "Clifford", "Miguel", "Oscar", "Jay", "Jim", "Tom", "Calvin", "Alex", "Jon",
								"Ronnie", "Bill", "Lloyd", "Tommy", "Leon", "Derek", "Warren", "Darrell", "Jerome", "Floyd", "Leo", "Alvin", "Tim", "Wesley", "Gordon", "Dean", "Greg", "Jorge", "Dustin", "Pedro", "Derrick", "Dan", "Lewis", "Zachary",
								"Corey", "Herman", "Maurice", "Vernon", "Roberto", "Clyde", "Glen", "Hector", "Shane", "Ricardo", "Sam", "Rick", "Lester", "Brent", "Ramon", "Charlie", "Tyler", "Gilbert", "Gene", "Marc", "Reginald", "Ruben", "Brett",
								"Angel", "Nathaniel", "Rafael", "Leslie", "Edgar", "Milton", "Raul", "Ben", "Chester", "Cecil", "Duane", "Franklin", "Andre", "Elmer", "Brad", "Gabriel", "Ron", "Mitchell", "Roland", "Arnold", "Harvey", "Jared", "Adrian",
								"Karl", "Cory", "Claude", "Erik", "Darryl", "Jamie", "Neil", "Jessie", "Christian", "Javier", "Fernando", "Clinton", "Ted", "Mathew", "Tyrone", "Darren", "Lonnie", "Lance", "Cody", "Julio", "Kelly", "Kurt", "Allan",
								"Nelson", "Guy", "Clayton", "Hugh", "Max", "Dwayne", "Dwight", "Armando", "Felix", "Jimmie", "Everett", "Jordan", "Ian", "Wallace", "Ken", "Bob", "Jaime", "Casey", "Alfredo", "Alberto", "Dave", "Ivan", "Johnnie", "Sidney",
								"Byron", "Julian", "Isaac", "Morris", "Clifton", "Willard", "Daryl", "Ross", "Virgil", "Andy", "Marshall", "Salvador", "Perry", "Kirk", "Sergio", "Marion", "Tracy", "Seth", "Kent", "Terrance", "Rene", "Eduardo",
								"Terrence", "Enrique", "Freddie", "Wade" };
						
						// Oooh
						final String[] NAMES_FEMALE = { "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty", "Helen", "Sandra", "Donna", "Carol", "Ruth", "Sharon",
								"Michelle", "Laura", "Sarah", "Kimberly", "Deborah", "Jessica", "Shirley", "Cynthia", "Angela", "Melissa", "Brenda", "Amy", "Anna", "Rebecca", "Virginia", "Kathleen", "Pamela", "Martha", "Debra", "Amanda", "Stephanie",
								"Carolyn", "Christine", "Marie", "Janet", "Catherine", "Frances", "Ann", "Joyce", "Diane", "Alice", "Julie", "Heather", "Teresa", "Doris", "Gloria", "Evelyn", "Jean", "Cheryl", "Mildred", "Katherine", "Joan", "Ashley",
								"Judith", "Rose", "Janice", "Kelly", "Nicole", "Judy", "Christina", "Kathy", "Theresa", "Beverly", "Denise", "Tammy", "Irene", "Jane", "Lori", "Rachel", "Marilyn", "Andrea", "Kathryn", "Louise", "Sara", "Anne",
								"Jacqueline", "Wanda", "Bonnie", "Julia", "Ruby", "Lois", "Tina", "Phyllis", "Norma", "Paula", "Diana", "Annie", "Lillian", "Emily", "Robin", "Peggy", "Crystal", "Gladys", "Rita", "Dawn", "Connie", "Florence", "Tracy",
								"Edna", "Tiffany", "Carmen", "Rosa", "Cindy", "Grace", "Wendy", "Victoria", "Edith", "Kim", "Sherry", "Sylvia", "Josephine", "Thelma", "Shannon", "Sheila", "Ethel", "Ellen", "Elaine", "Marjorie", "Carrie", "Charlotte",
								"Monica", "Esther", "Pauline", "Emma", "Juanita", "Anita", "Rhonda", "Hazel", "Amber", "Eva", "Debbie", "April", "Leslie", "Clara", "Lucille", "Jamie", "Joanne", "Eleanor", "Valerie", "Danielle", "Megan", "Alicia",
								"Suzanne", "Michele", "Gail", "Bertha", "Darlene", "Veronica", "Jill", "Erin", "Geraldine", "Lauren", "Cathy", "Joann", "Lorraine", "Lynn", "Sally", "Regina", "Erica", "Beatrice", "Dolores", "Bernice", "Audrey", "Yvonne",
								"Annette", "June", "Samantha", "Marion", "Dana", "Stacy", "Ana", "Renee", "Ida", "Vivian", "Roberta", "Holly", "Brittany", "Melanie", "Loretta", "Yolanda", "Jeanette", "Laurie", "Katie", "Kristen", "Vanessa", "Alma",
								"Sue", "Elsie", "Beth", "Jeanne", "Vicki", "Carla", "Tara", "Rosemary", "Eileen", "Terri", "Gertrude", "Lucy", "Tonya", "Ella", "Stacey", "Wilma", "Gina", "Kristin", "Jessie", "Natalie", "Agnes", "Vera", "Willie",
								"Charlene", "Bessie", "Delores", "Melinda", "Pearl", "Arlene", "Maureen", "Colleen", "Allison", "Tamara", "Joy", "Georgia", "Constance", "Lillie", "Claudia", "Jackie", "Marcia", "Tanya", "Nellie", "Minnie", "Marlene",
								"Heidi", "Glenda", "Lydia", "Viola", "Courtney", "Marian", "Stella", "Caroline", "Dora", "Jo", "Vickie", "Mattie", "Terry", "Maxine", "Irma", "Mabel", "Marsha", "Myrtle", "Lena", "Christy", "Deanna", "Patsy", "Hilda",
								"Gwendolyn", "Jennie", "Nora", "Margie", "Nina", "Cassandra", "Leah", "Penny", "Kay", "Priscilla", "Naomi", "Carole", "Brandy", "Olga", "Billie", "Dianne", "Tracey", "Leona", "Jenny", "Felicia", "Sonia", "Miriam", "Velma",
								"Becky", "Bobbie", "Violet", "Kristina", "Toni", "Misty", "Mae", "Shelly", "Daisy", "Ramona", "Sherri", "Erika", "Katrina", "Claire" };
						
						// Create all the bots
						Random random = Environment.getRandom();
						for (int num = 0; num < amount; num++)
						{
							// Create user info
							User info = HabboHotel.getUserRegister().newUser();
							info.ID = -1;
							if (arg[2].equals("self"))
							{
								info.figure = comm.getUserObject().figure;
								info.sex = comm.getUserObject().sex;
							}
							else
							{
								// Determine sex for random figure
								char sex;
								if (arg[2].equals("rnd"))
								{
									sex = (random.nextBoolean() ? 'M' : 'F');
								}
								else
								{
									sex = (arg[2].equals("f") ? 'F' : 'M');
								}
								
								info.figure = FigureUtil.generateRandomFigure(sex);
								info.sex = sex;
							}
							info.motto = "";
							
							// Determine name
							if (info.sex == 'M')
							{
								info.name = "@" + NAMES_MALE[random.nextInt(NAMES_MALE.length)];
							}
							else
							{
								info.name = "@" + NAMES_FEMALE[random.nextInt(NAMES_FEMALE.length)];
							}
							info.name += random.nextInt(999);
							
							// Create bot
							SpaceBot bot = new SpaceBot(info);
							bot.X = comm.getSpaceInstance().getModel().doorX;
							bot.Y = comm.getSpaceInstance().getModel().doorY;
							bot.ownerID = comm.getUserObject().ID;
							
							// Add bot
							comm.getSpaceInstance().addBot(bot);
						}
					}
					else
					{
						comm.systemMsg("Please specify the number of bots to spawn.");
					}
				}
				else if (arg[0].equals("clear"))
				{
					comm.getSpaceInstance().clearBots();
				}
				else if (arg[0].equals("setmode"))
				{
					if (arg.length == 2)
					{
						byte newMode = SpaceBotMode.INVALID;
						if (arg[1].equals("idle"))
						{
							newMode = SpaceBotMode.IDLE;
						}
						else if (arg[1].equals("ai_default"))
						{
							newMode = SpaceBotMode.AI_DEFAULT;
						}
						else if (arg[1].equals("ai_angry"))
						{
							newMode = SpaceBotMode.AI_ANGRY;
						}
						else if (arg[1].equals("ai_scared"))
						{
							newMode = SpaceBotMode.AI_SCARED;
						}
						else if (arg[1].equals("stress"))
						{
							newMode = SpaceBotMode.STRESS;
						}
						else if (arg[1].equals("stress_pf"))
						{
							newMode = SpaceBotMode.STRESS_PATHFINDING;
						}
						else
						{
							comm.systemMsg("Invalid mode. Valid modes are: 'idle', 'ai', 'stress', 'stress_pf'.\rUsing 'idle'.");
							newMode = SpaceBotMode.IDLE;
						}
						
						for (SpaceBot bot : comm.getSpaceInstance().getBots())
						{
							bot.setMode(newMode);
						}
					}
				}
				else if (arg[0].equals("shout"))
				{
					arg = body.split(" ", 2);
					if (arg.length == 2)
					{
						for (SpaceBot bot : comm.getSpaceInstance().getBots())
						{
							comm.getSpaceInstance().chat(bot, arg[1], true);
						}
					}
				}
				else if (arg[0].equals("move"))
				{
					if (arg.length == 3)
					{
						short xAdd = Short.parseShort(arg[1]);
						short yAdd = Short.parseShort(arg[2]);
						
						for (SpaceBot bot : comm.getSpaceInstance().getBots())
						{
							short X = (short)(bot.X + xAdd);
							short Y = (short)(bot.Y + yAdd);
							comm.getSpaceInstance().getInteractor().startUserMovement(bot, X, Y, false);
						}
					}
				}
				else if (arg[0].equals("magnet"))
				{
					SpaceUser target;
					if (arg.length == 2)
					{
						target = comm.getSpaceInstance().getUserByName(arg[1]);
					}
					else
					{
						target = comm.getSpaceInstance().getUserByClientID(comm.clientID);
					}
					
					short X = (short)(target.X - 1);
					short Y = (short)(target.Y + 1);
					for (SpaceBot bot : comm.getSpaceInstance().getBots())
					{
						comm.getSpaceInstance().getInteractor().startUserMovement(bot, X, Y, false);
					}
				}
				else if (arg[0].equals("wave"))
				{
					for (SpaceBot bot : comm.getSpaceInstance().getBots())
					{
						bot.wave();
					}
				}
				else if (arg[0].equals("dance"))
				{
					for (SpaceBot bot : comm.getSpaceInstance().getBots())
					{
						bot.removeStatus("carryd");
						bot.addStatus("dance", null, 0, null, 0, 0);
					}
				}
				else if (arg[0].equals("moonwalk"))
				{
					for (SpaceBot bot : comm.getSpaceInstance().getBots())
					{
						bot.isReverseWalk = !bot.isReverseWalk;
					}
				}
				else if (arg[0].equals("lookto"))
				{
					if (arg.length == 2)
					{
						SpaceUser target = comm.getSpaceInstance().getUserByName(arg[1]);
						if (target != null)
						{
							for (SpaceBot bot : comm.getSpaceInstance().getBots())
							{
								if (!bot.hasStatus("sit") && !bot.hasStatus("lay"))
								{
									bot.headRotation = RotationCalculator.calculateHumanDirection(bot.X, bot.Y, target.X, target.Y);
									bot.bodyRotation = bot.headRotation;
									bot.ensureUpdate(true);
								}
							}
						}
					}
				}
				else if (arg[0].equals("carrydrink"))
				{
					for (SpaceBot bot : comm.getSpaceInstance().getBots())
					{
						if (!bot.hasStatus("sit") && !bot.hasStatus("lay"))
						{
							bot.removeStatus("dance");
							bot.removeStatus("drink");
							bot.addStatus("carryd", arg[1], 120, "drink", 12, 1);
						}
					}
				}
				else
				{
					comm.systemMsg("Invalid argument \"" + arg[0] + "\".");
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean handleMyBots(CommunicationHandler comm, String body)
	{
		// Owner of this room?
		if (comm.getUserObject().ID != comm.getSpaceInstance().getInfo().ownerID && !comm.getUserObject().hasRight("can_control_bots"))
		{
			return false;
		}
		
		if (body == null)
		{
			ServerMessage msg = new ServerMessage("SYSTEMBROADCAST");
			msg.appendArgument("You have the following bots in this room:");
			for (SpaceBot bot : comm.getSpaceInstance().getBots())
			{
				msg.appendNewArgument(bot.getUserObject().name);
			}
			comm.sendMessage(msg);
		}
		else if (body.equals("reload"))
		{
			// Reload the bots from database
			comm.getSpaceInstance().clearBots();
			comm.getSpaceInstance().loadBots();
			comm.systemMsg("Reloaded your bots!");
		}
		else
		{
			String parts[] = body.split(" ", 2);
			if (parts.length != 2)
			{
				comm.systemMsg("Help on this command:\r-:mybots reload\tReloads your bots in this room\r:mybots %name% %text%\tMakes bot %name% say %text%");
			}
			else
			{
				parts[0] = "~" + parts[0];
				for (SpaceBot bot : comm.getSpaceInstance().getBots())
				{
					if (bot.getUserObject().name.equalsIgnoreCase(parts[0]))
					{
						comm.getSpaceInstance().chat(bot, parts[1], true);
						return true;
					}
				}
				comm.systemMsg("You haven't got a bot named '" + parts[0] + "' in this room!");
			}
		}
		
		/// Nuhohh
		return true;
	}
	
	public static boolean handleTransfer(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_transfer_user"))
		{
			if (body != null)
			{
				CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(body);
				if (client == null)
				{
					comm.systemMsg("Target user \"" + body + "\" not logged in.");
				}
				else
				{
					client.systemMsg("You are being transfered to \"" + comm.getSpaceInstance().getInfo().name + "\" by " + comm.getUserObject().name + ".");
					
					client.sendMessage(new ServerMessage("ADVERTISEMENT 0"));
					client.sendMessage(new ServerMessage("OPC_OK"));
					client.goToSpace(comm.getSpaceInstance().getInfo().ID);
				}
			}
			else
			{
				comm.systemMsg("Please specify a user name.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean handleShellEvaluation(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_use_blunkshell"))
		{
			if (body != null)
			{
				// Create shell instance
				BlunkShell sh = new BlunkShell();
				sh.setVariable("comm", comm);
				sh.setVariable("args", new String[0]);
				body = "import com.blunk.*; import com.suelake.habbo.*; " + body;
				
				try
				{
					Object res = sh.evaluate(body);
					comm.systemMsg("BlunkShell: evaluated input successfully, return value: " + res);
				}
				catch (Exception ex)
				{
					ServerMessage wnd = new ServerMessage("SYSTEMBROADCAST");
					wnd.appendNewArgument("Blunk Shell: an error occurred! Exception details:");
					for (String line : ChatCommandHandler.wordWrap(ex.getMessage()))
					{
						wnd.appendNewArgument(line);
					}
					comm.sendMessage(wnd);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public static boolean handleShellCall(CommunicationHandler comm, String body)
	{
		if (comm.getUserObject().hasRight("can_use_blunkshell"))
		{
			if (body != null)
			{
				// Create shell instance
				BlunkShell sh = new BlunkShell();
				sh.setVariable("comm", comm);
				
				// Parse the chat input arguments
				String[] chatArgs = body.split(" ", 2); // [0] = scriptname, [1] = args separated by '~'
				if (chatArgs.length == 1)
				{
					sh.setVariable("args", new String[0]);
				}
				else
				{
					sh.setVariable("args", chatArgs[1].split("~"));
				}
				
				try
				{
					// Evaluate the remote script %scriptname%
					Object res = sh.evaluateRemoteScript(chatArgs[0]);
					comm.systemMsg("BlunkShell: evaluated input successfully, return value: " + res);
				}
				catch (Exception ex)
				{
					ServerMessage wnd = new ServerMessage("SYSTEMBROADCAST");
					wnd.appendNewArgument("Blunk Shell: an error occurred! Exception details:");
					for (String line : ChatCommandHandler.wordWrap(ex.getMessage()))
					{
						wnd.appendNewArgument(line);
					}
					comm.sendMessage(wnd);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	private static String[] wordWrap(String str)
	{
		final Pattern wrapRE = Pattern.compile(".{0,79}(?:\\S(?:-| |$)|$)");
		
		List<String> list = new LinkedList<String>();
		
		Matcher m = wrapRE.matcher(str);
		
		while (m.find())
		{
			list.add(m.group());
		}
		
		return (String[])list.toArray(new String[list.size()]);
		
	}
}
