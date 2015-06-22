package com.suelake.habbo.communication;

import com.blunk.Log;

public class ClientRequestHandlerManager
{
	private CommunicationHandler m_comm;
	private boolean[] m_registered;
	
	public ClientRequestHandlerManager(CommunicationHandler comm)
	{
		m_comm = comm;
		m_registered = new boolean[ClientCommands.max + 1];
	}
	
	public void registerRequestHandler(int commandID, boolean state)
	{
		m_registered[commandID] = state;
	}
	
	public void handleRequest(ClientMessage msg)
	{
		if (!ClientCommands.handleRequest(m_comm, msg, m_registered))
		{
			Log.debug("No ClientRequestHandler in client " + m_comm.clientID + " for command " + msg.getType());
		}
	}
	
	public void callHandler(int commandID)
	{
		ClientCommands.invokeHandler(m_comm, commandID);
	}
	
	// Handler registering
	
	public void registerSecurityHandlers(boolean state)
	{
		m_registered[ClientCommands.VERSIONCHECK] = state;
		m_registered[ClientCommands.KEYENCRYPTED] = state;
	}
	
	public void registerGlobalHandlers(boolean state)
	{
		// These handlers are available as soon as crypto = OK, and will be always available
		m_registered[ClientCommands.UNIQUEMACHINEID] = state;
		m_registered[ClientCommands.GETAVAILABLESETS] = state;
		m_registered[ClientCommands.APPROVENAME] = state;
		m_registered[ClientCommands.FINDUSER] = state;
		m_registered[ClientCommands.QUIT] = state;
		
		// Client will call this nomatter what connection state, so ... yeah
		m_registered[ClientCommands.MESSENGER_SENDUPDATE] = state;
	}
	
	public void registerPreLoginHandlers(boolean state)
	{
		m_registered[ClientCommands.LOGIN] = state;
		m_registered[ClientCommands.REGISTER] = state;
		m_registered[ClientCommands.SEND_USERPASS_TO_EMAIL] = state;
	}
	
	public void registerUserHandlers(boolean state)
	{
		m_registered[ClientCommands.INFORETRIEVE] = state;
		m_registered[ClientCommands.GETCREDITS] = state;
		m_registered[ClientCommands.MESSENGERINIT] = state;
		m_registered[ClientCommands.SCR_GINFO] = state;
		m_registered[ClientCommands.UPDATE] = state;
		m_registered[ClientCommands.SCR_SUBSCRIBE] = state;
		m_registered[ClientCommands.SCR_EXTSCR] = state;
		m_registered[ClientCommands.CRYFORHELP] = state;
		
		m_registered[ClientCommands.GETADFORME] = state;
		m_registered[ClientCommands.BTCKS] = state;
		m_registered[ClientCommands.GCIX] = state;
		m_registered[ClientCommands.GCAP] = state;
		m_registered[ClientCommands.GPRC] = state;
		m_registered[ClientCommands.GETSTRIP] = state;
	}
	
	public void registerMessengerHandlers(boolean state)
	{
		m_registered[ClientCommands.MESSENGER_SENDUPDATE] = state;
		m_registered[ClientCommands.MESSENGER_SENDMSG] = state;
		m_registered[ClientCommands.MESSENGER_MARKREAD] = state;
		m_registered[ClientCommands.MESSENGER_ASSIGNPERSMSG] = state;
		m_registered[ClientCommands.MESSENGER_REQUESTBUDDY] = state;
		m_registered[ClientCommands.MESSENGER_REMOVEBUDDY] = state;
		m_registered[ClientCommands.MESSENGER_ACCEPTBUDDY] = state;
		m_registered[ClientCommands.MESSENGER_DECLINEBUDDY] = state;
	}
	
	public void registerNavigatorHandlers(boolean state)
	{
		m_registered[ClientCommands.GETALLUNITS] = state;
		m_registered[ClientCommands.GETUNITUPDATES] = state;
		m_registered[ClientCommands.GETUNITUSERS] = state;
		m_registered[ClientCommands.SBUSYF] = state;
		m_registered[ClientCommands.SUSERF] = state;
		m_registered[ClientCommands.SRCHF] = state;
		m_registered[ClientCommands.GETFVRF] = state;
		m_registered[ClientCommands.ADD_FAVORITE_ROOM] = state;
		m_registered[ClientCommands.DEL_FAVORITE_ROOM] = state;
		m_registered[ClientCommands.CREATEFLAT] = state;
		m_registered[ClientCommands.DELETEFLAT] = state;
		m_registered[ClientCommands.UPDATEFLAT] = state;
		m_registered[ClientCommands.GETFLATINFO] = state;
		m_registered[ClientCommands.SETFLATINFO] = state;
		
		// Flat entering handlers
		m_registered[ClientCommands.TRYFLAT] = state;
		m_registered[ClientCommands.GOTOFLAT] = state;
		m_registered[ClientCommands.GOVIADOOR] = state;
	}
	
	public void registerSpaceHandlers(boolean state)
	{
		m_registered[ClientCommands.G_HMAP] = state;
		m_registered[ClientCommands.G_USRS] = state;
		m_registered[ClientCommands.G_OBJS] = state;
		m_registered[ClientCommands.G_STAT] = state;
		
		// Chat
		m_registered[ClientCommands.CHAT] = state;
		m_registered[ClientCommands.SHOUT] = state;
		m_registered[ClientCommands.WHISPER] = state;
		
		// Actions
		m_registered[ClientCommands.MOVE] = state;
		m_registered[ClientCommands.DANCE] = state;
		m_registered[ClientCommands.WAVE] = state;
		m_registered[ClientCommands.CARRYDRINK] = state;
		m_registered[ClientCommands.CARRYITEM] = state;
		m_registered[ClientCommands.USEITEM] = state;
		m_registered[ClientCommands.LOOKTO] = state;
		m_registered[ClientCommands.STOP] = state;
		m_registered[ClientCommands.GOAWAY] = state;
		
		// Badge
		m_registered[ClientCommands.SHOWBADGE] = state;
		m_registered[ClientCommands.HIDEBADGE] = state;
	}
	
	public void registerUserFlatHandlers(boolean state)
	{
		m_registered[ClientCommands.G_ITEMS] = state;
		
		m_registered[ClientCommands.KILLUSER] = state;
		m_registered[ClientCommands.ASSIGNRIGHTS] = state;
		m_registered[ClientCommands.REMOVERIGHTS] = state;
		m_registered[ClientCommands.LETUSERIN] = state;
		
		// Place and delete
		m_registered[ClientCommands.PLACESTUFFFROMSTRIP] = state;
		m_registered[ClientCommands.REMOVESTUFF] = state;
		m_registered[ClientCommands.PLACEITEMFROMSTRIP] = state;
		m_registered[ClientCommands.REMOVEITEM] = state;
		
		// Pickup and move
		m_registered[ClientCommands.ADDSTRIPITEM] = state;
		m_registered[ClientCommands.MOVESTUFF] = state;
		
		// Set data
		m_registered[ClientCommands.SETSTUFFDATA] = state;
		m_registered[ClientCommands.SETITEMDATA] = state;
		m_registered[ClientCommands.G_IDATA] = state;
		m_registered[ClientCommands.FLATPROPERTYBYITEM] = state;
		
		// Trading
		m_registered[ClientCommands.TRADE_OPEN] = state;
		m_registered[ClientCommands.TRADE_CLOSE] = state;
		m_registered[ClientCommands.TRADE_ADDITEM] = state;
		m_registered[ClientCommands.TRADE_ACCEPT] = state;
		m_registered[ClientCommands.TRADE_UNACCEPT] = state;
		
		// Dice
		m_registered[ClientCommands.DICE_OFF] = state;
		m_registered[ClientCommands.THROW_DICE] = state;
		
		// Teleporter
		m_registered[ClientCommands.INTODOOR] = state;
		m_registered[ClientCommands.DOORGOIN] = state;
		m_registered[ClientCommands.GETDOORFLAT] = state;
		
		// Presents
		m_registered[ClientCommands.PRESENTOPEN] = state;
	}
	
	public void registerSwimmingPoolHandlers(boolean state)
	{
		m_registered[ClientCommands.CLOSE_UIMAKOPPI] = state;
		m_registered[ClientCommands.SIGN] = state;
		m_registered[ClientCommands.JUMPSTART] = state;
		m_registered[ClientCommands.JUMPPERF] = state;
		m_registered[ClientCommands.SPLASH_POSITION] = state;
		m_registered[ClientCommands.PTM] = state;
	}
	
	public void registerModerationHandlers(boolean state)
	{
		m_registered[ClientCommands.PICK_CRYFORHELP] = state;
	}
}
