package com.suelake.habbo.communication;

import java.util.Vector;

import com.blunk.Log;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.moderation.ModerationBan;
import com.suelake.habbo.net.InfoConnection;

public class ClientManager implements Runnable
{
	private final static int MAX_MESSAGEDELAY_SECONDS = 120;
	
	private int m_maxClients;
	private int m_clientCounter;
	private Thread m_monitor;
	private Vector<CommunicationHandler> m_clients;
	
	public ClientManager(int maxClients)
	{
		m_maxClients = (maxClients > 0) ? maxClients : -1;
		m_clients = new Vector<CommunicationHandler>();
		m_monitor = new Thread(this, "ClientManager");
		m_monitor.setPriority(Thread.MIN_PRIORITY);
	}
	
	public void startMonitor()
	{
		m_monitor.start();
	}
	
	public void stopMonitor()
	{
		m_monitor.interrupt();
	}
	
	public void handleAcceptedConnection(InfoConnection connection)
	{
		// Allow new connection or refuse it?
		if(m_clients.size() >= m_maxClients)
		{
			Log.info("ClientManager: refusing client from " + connection.getIpAddress() + ", server is full!");
			connection.stop();
			return;
		}
		
		// Create client
		CommunicationHandler client = new CommunicationHandler(++m_clientCounter, connection, this);
		if (connection.start(client))
		{
			Log.info("ClientManager: accepted client #" + client.clientID + " from " + connection.getIpAddress() + " [" + connection.getHostName() + "]");
			
			// Check if this IP is banned
			ModerationBan ban = HabboHotel.getModerationCenter().getIpBan(connection.getIpAddress());
			if (ban == null)
			{
				// Connection welcome!
				synchronized (m_clients)
				{
					m_clients.add(client);
				}
				
				// Setup connection
				client.start();
			}
			else
			{
				// Notify & disconnect client
				client.sendBan(ban);
				client.stop("IP is banned");
			}
		}
		else
		{
			Log.error("ClientManager: could not start & link newly accepted InfoConnection and CommunicationHandler! Connection refused.");
		}
	}
	
	public void run()
	{
		while (true)
		{
			// Do events!
			
			// Drop timed out clients (no recent message traffic)
			this.dropTimedOutClients();
			
			// Other stuff (eg, make a fancy graph of online users amount etc)
			
			// Sleep
			try
			{
				Thread.sleep(60 * 1000);
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
	}
	
	public boolean removeClient(CommunicationHandler client, String reason)
	{
		Log.info("ClientManager: removed client #" + client.clientID + " [reason: " + reason + "]");
		synchronized (m_clients)
		{
			return m_clients.remove(client);
		}
	}
	
	public int dropTimedOutClients()
	{
		// Determine current time
		long nowTime = TimeHelper.getTime();
		
		// Gather the timed out clients
		Vector<CommunicationHandler> selection = new Vector<CommunicationHandler>();
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				// Has this client timed out?
				if ((TimeHelper.getTime() - client.getLastMessageTime()) > (ClientManager.MAX_MESSAGEDELAY_SECONDS * 1000))
				{
					selection.add(client);
				}
			}
		}
		
		// Disconnect the timed out clients
		for (CommunicationHandler client : selection)
		{
			client.stop("timed out");
		}
		
		return selection.size();
	}
	
	public CommunicationHandler getClient(int clientID)
	{
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.clientID == clientID)
				{
					return client;
				}
			}
		}
		
		return null;
	}
	
	public CommunicationHandler getClientOfUser(int userID)
	{
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.getUserObject() != null)
				{
					if (client.getUserObject().ID == userID)
					{
						return client;
					}
				}
			}
		}
		
		return null;
	}
	
	public CommunicationHandler getClientOfUser(String userName)
	{
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.getUserObject() != null)
				{
					if (client.getUserObject().name.equalsIgnoreCase(userName))
					{
						return client;
					}
				}
			}
		}
		
		return null;
	}
	
	public Vector<CommunicationHandler> getClientsWithUserRole(byte minimumRole)
	{
		Vector<CommunicationHandler> selection = new Vector<CommunicationHandler>();
		
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.getUserObject() != null && client.getUserObject().role >= minimumRole)
				{
					selection.add(client);
				}
			}
		}
		
		return selection;
	}
	
	public boolean disconnectUser(int userID, String reason)
	{
		CommunicationHandler client = this.getClientOfUser(userID);
		if (client != null)
		{
			client.stop(reason);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Vector<CommunicationHandler> getClientsWithIpAddress(String ipAddress)
	{
		Vector<CommunicationHandler> selection = new Vector<CommunicationHandler>();
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.getConnection().getIpAddress().equals(ipAddress))
				{
					selection.add(client);
				}
			}
		}
		
		return selection;
	}
	
	public Vector<CommunicationHandler> getLoggedInClients()
	{
		Vector<CommunicationHandler> selection = new Vector<CommunicationHandler>();
		synchronized (m_clients)
		{
			for (CommunicationHandler client : m_clients)
			{
				if (client.getUserObject() != null)
				{
					selection.add(client);
				}
			}
		}
		
		return selection;
	}
	
	public Vector<CommunicationHandler> getClients()
	{
		return m_clients;
	}
	
	public int count()
	{
		return m_clients.size();
	}
	
	public int max()
	{
		return m_maxClients;
	}
}
