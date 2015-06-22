package com.suelake.habbo.spaces.instances;

import java.util.Vector;

import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.spaces.Space;
import com.suelake.habbo.spaces.SpaceModel;

/**
 * SpaceInstanceServer monitors running SpaceInstance's and knows how to instantiate new ones.
 * 
 * @author Nillus
 */
public class SpaceInstanceServer
{
	private final int m_serverID;
	private final int m_maxInstances;
	private Vector<SpaceInstance> m_instances;
	
	public SpaceInstanceServer(int serverID, int maxInstances)
	{
		m_serverID = serverID;
		m_maxInstances = maxInstances;
		m_instances = new Vector<SpaceInstance>(maxInstances / 4);
	}
	
	/**
	 * Destroys all running SpaceInstances.
	 */
	public void destroy()
	{
		synchronized (m_instances)
		{
			// Stop and destroy all instances
			for (SpaceInstance instance : m_instances)
			{
				instance.destroy();
			}
			
			// Clear collection
			m_instances.clear();
		}
		Log.info("SpaceInstanceServer [" + m_serverID + "] was stopped.");
	}
	
	/**
	 * Used internally to create a new SpaceInstance based on a given Space DataObject. The instance will be initialized and model will be set etc.
	 * 
	 * @param info The Space DataObject holding details of the space to create an instance of.
	 * @return The SpaceInstance object if successful, null otherwise.
	 */
	private SpaceInstance createInstance(Space info)
	{
		if (info != null)
		{
			try
			{
				// Get model
				SpaceModel model = HabboHotel.getSpaceAdmin().getModels().getModel(info.model);
				
				// Instantiate instance, load flat controllers and generate maps
				SpaceInstance instance = new SpaceInstance(info, model);
				instance.reloadFlatControllers();
				instance.getInteractor().loadItems();
				instance.getInteractor().generateFloorMap(true); // Map passive objects: true
				
				// Load bots
				instance.loadBots();
				
				return instance;
			}
			catch (Exception ex)
			{
				Log.error("SpaceInstanceServer [" + m_serverID + "] could not create an instance of space " + info.ID + ", error while instantiating space and generating maps", ex);
			}
		}
		
		// Could not create instance for whatever reason
		return null;
	}
	
	/**
	 * Returns a running instance of a given space.
	 * 
	 * @param spaceID The database ID of the space to return the instance of.
	 * @return The instance of the space. NULL if there is no SpaceInstance with the given ID running in this SpaceInstanceServer.
	 */
	public SpaceInstance getInstance(int spaceID)
	{
		// Already a instance started?
		synchronized (m_instances)
		{
			for (SpaceInstance instance : m_instances)
			{
				if (instance.getInfo().ID == spaceID)
				{
					return instance;
				}
			}
		}
		
		// Not here!
		return null;
	}
	
	/**
	 * Attempts to destroy the instance of a given space ID.
	 * 
	 * @param spaceID The database ID of the space to destroy the instance of.
	 */
	public boolean destroyInstance(int spaceID)
	{
		SpaceInstance instance = this.getInstance(spaceID);
		if (instance != null)
		{
			// Stop interactor and destroy room
			instance.getInteractor().stop();
			instance.getInteractor().clear();
			instance.destroy();
			synchronized(m_instances)
			{
				m_instances.removeElement(instance);
			}
			
			// Log activity
			Log.info("SpaceInstanceServer [" + m_serverID + "] destroyed instance of space " + spaceID);
			
			// OK!
			return true;
		}
		else
		{
			// Not running!
			return false;
		}
	}
	
	/**
	 * Attempts to start a new SpaceInstance at this SpaceInstanceServer.
	 * 
	 * @param spaceID The database ID of the Space to start a SpaceInstance for.
	 * @return The SpaceInstance object if succeeded, NULL otherwise.
	 */
	public SpaceInstance startInstance(int spaceID)
	{
		// Load info from database
		Space info = HabboHotel.getSpaceAdmin().getSpaceInfo(spaceID);
		
		// Try to create instance
		SpaceInstance instance = this.createInstance(info);
		if (instance != null)
		{
			// Start instance worker thread
			instance.getInteractor().start();
			
			// Add to instances collection
			synchronized(m_instances)
			{
			m_instances.add(instance);
			}
			
			// Log activity
			Log.info("SpaceInstanceServer [" + m_serverID + "] started new instance of space " + info.ID);
			
			// Return the new instance
			return instance;
		}
		else
		{
			// Could not start!
			return null;
		}
	}
	
	/**
	 * Searches for a running public space instance with a given name.
	 * 
	 * @param name The name of the public space to get the instance of. Case sensitive.
	 * @return SpaceInstance when found, null otherwise.
	 */
	public SpaceInstance getPublicSpaceByName(String name)
	{
		synchronized (m_instances)
		{
			for (SpaceInstance instance : m_instances)
			{
				if (!instance.getInfo().isUserFlat() && instance.getInfo().name.equals(name))
				{
					return instance;
				}
			}
		}
		
		return null;
	}
	
	public Vector<SpaceInstance> getBusyFlats()
	{
		Vector<SpaceInstance> busy = new Vector<SpaceInstance>();
		synchronized(m_instances)
		{
		for (SpaceInstance instance : m_instances)
		{
			if (instance.getInfo().isUserFlat())
			{
				if (instance.userAmount() > 0)
				{
					busy.add(instance);
				}
			}
		}
		}
		
		return busy;
	}
	
	/**
	 * Returns the current amount of running SpaceInstances in this SpaceInstanceServer.
	 */
	public int instanceAmount()
	{
		synchronized(m_instances)
		{
			return m_instances.size();
		}
	}
	
	/**
	 * Returns a floating point value that compares the current instance amount against the maximum instance amount.
	 * 
	 * @return The factor. 0 means there are no instances loaded at all, 1 means that it is at maximum capacity.
	 */
	public float loadFactor()
	{
		synchronized(m_instances)
		{
			return m_instances.size() / m_maxInstances;
		}
	}
}