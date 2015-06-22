package com.suelake.habbo.spaces.instances;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import com.blunk.Log;

/**
 * SpaceDirectory is a collection of remote references (RMI) to SpaceInstanceServers.\r
 * SpaceDirectory is able to find SpaceInstances on the servers it has registered and can determine where to start new SpaceInstances.
 * 
 * @author Nillus
 */
public class SpaceDirectory implements Remote
{
	private SpaceInstanceServer[] m_servers;
	
	public SpaceDirectory(int serverAmount)
	{
		m_servers = new SpaceInstanceServer[serverAmount];
	}
	
	public boolean addServer(int serverID, int maxInstances, String rmiUrl)
	{
		if (serverID >= 0 && serverID < m_servers.length)
		{
			if (m_servers[serverID] == null)
			{
				if (rmiUrl.equals("internal"))
				{
					m_servers[serverID] = new SpaceInstanceServer(serverID, maxInstances);
				}
				else
				{
					try
					{
						m_servers[serverID] = (SpaceInstanceServer)Naming.lookup(rmiUrl);
						return true;
					}
					catch (MalformedURLException ex)
					{
						Log.error("The rmi url of SpaceInstanceServer " + serverID + " [\"" + rmiUrl + "\"] was misformed!", ex);
					}
					catch (RemoteException ex)
					{
						Log.error("A RemoteException occurred during resolving of SpaceInstanceServer " + serverID + " at \"" + rmiUrl + "\"!", ex);
					}
					catch (NotBoundException ex)
					{
						Log.error("There is no SpaceInstanceServer started and bound to \"" + rmiUrl + "\"!", ex);
					}
				}
			}
		}
		
		// Failed!
		return false;
	}
	
	public boolean removeServer(int serverID)
	{
		if (serverID >= 0 && serverID < m_servers.length)
		{
			if (m_servers[serverID] != null)
			{
				// destroy
				m_servers[serverID] = null;
				return true;
			}
		}
		
		// No server
		return false;
	}
	
	public SpaceInstanceServer getServer(int serverID)
	{
		if (serverID >= 0 && serverID < m_servers.length)
		{
			return m_servers[serverID];
		}
		
		return null;
	}
	
	public SpaceInstance getInstance(int spaceID, boolean allowNew)
	{
		// First check for running instances on all servers
		for (int i = 0; i < m_servers.length; i++)
		{
			if (m_servers[i] != null)
			{
				SpaceInstance instance = m_servers[i].getInstance(spaceID);
				if (instance != null)
				{
					return instance;
				}
			}
		}
		
		// No running instance, must create new one. Allowed?		
		if (allowNew)
		{
			// Try to create one on the server with the lowest load factor
			SpaceInstanceServer server = null;
			float loadFactor = 1.0f;
			for (int i = 0; i < m_servers.length; i++)
			{
				if (m_servers[i] != null)
				{
					if (m_servers[i].loadFactor() < loadFactor)
					{
						server = m_servers[i];
						loadFactor = server.loadFactor();
					}
				}
			}
			
			// Suitable server located?
			if (server != null)
			{
				// Create instance here
				SpaceInstance instance = server.startInstance(spaceID);
				if (instance != null)
				{
					return instance;
				}
			}
		}
		
		// All servers are full!
		return null;
	}
	
	public boolean destroyInstance(int spaceID)
	{
		for (int i = 0; i < m_servers.length; i++)
		{
			if (m_servers[i].destroyInstance(spaceID))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public SpaceInstance getUnitByName(String name)
	{
		for (int i = 0; i < m_servers.length; i++)
		{
			SpaceInstance instance = m_servers[i].getPublicSpaceByName(name);
			if (instance != null)
			{
				return instance;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the amount of Users in a Space.
	 * 
	 * @param spaceID The database ID of the Space to get the User amount of.
	 * @return The amount of Users in the Space.
	 */
	public int getUserAmount(int spaceID)
	{
		SpaceInstance instance = this.getInstance(spaceID, false);
		return (instance != null) ? instance.userAmount() : 0;
	}
	
	public Vector<SpaceInstance> getBusyFlats()
	{
		Vector<SpaceInstance> busy = new Vector<SpaceInstance>();
		for (int i = 0; i < m_servers.length; i++)
		{
			busy.addAll(m_servers[i].getBusyFlats());
		}
		
		// Sort by user amount descending
		//java.util.Collections.sort(busy);
		
		return busy;
	}
}
