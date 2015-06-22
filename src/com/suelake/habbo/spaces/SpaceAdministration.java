package com.suelake.habbo.spaces;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.spaces.instances.SpaceBot;
import com.suelake.habbo.spaces.instances.SpaceInstance;

/**
 * SpaceAdministration provides methods for inserting, creating and editing Space objects in the Database, aswell as providing other misc tasks on Spaces such as rights in user flats etc.
 * 
 * @author Nils
 */
public class SpaceAdministration
{
	@SuppressWarnings("unchecked")
	private Class m_spaceClass;
	private SpaceModelManager m_modelManager;
	
	public SpaceAdministration()
	{
		Space sample = (Space)HabboHotel.getDataObjectFactory().newObject("Space");
		if (sample != null)
		{
			m_spaceClass = sample.getClass();
		}
		
		m_modelManager = new SpaceModelManager();
	}
	
	public Space getSpaceInfo(int spaceID)
	{
		SpaceInstance instance = HabboHotel.getSpaceDirectory().getInstance(spaceID, false);
		if (instance != null)
		{
			return instance.getInfo();
		}
		else
		{
			Space space = this.newSpace();
			space.ID = spaceID;
			if (Environment.getDatabase().load(space))
			{
				return space;
			}
			else
			{
				return null;
			}
		}
	}
	
	public void updateSpaceInfo(Space space)
	{
		// TODO: cache?
		
		Environment.getDatabase().update(space);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Space> findPublicSpaces()
	{
		PublicSpaceFinder finder = (PublicSpaceFinder)HabboHotel.getDataQueryFactory().newQuery("PublicSpaceFinder");
		
		return (Vector<Space>)Environment.getDatabase().query(finder);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Space> findFlatsForUser(int userID)
	{
		UserFlatFinder finder = (UserFlatFinder)HabboHotel.getDataQueryFactory().newQuery("UserFlatFinder");
		finder.searchByUser = true;
		finder.userID = userID;
		
		return (Vector<Space>)Environment.getDatabase().query(finder);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Space> findBusyFlats(int start, int stop)
	{
		UserFlatFinder finder = (UserFlatFinder)HabboHotel.getDataQueryFactory().newQuery("UserFlatFinder");
		finder.searchBusy = true;
		finder.start = start;
		finder.stop = stop;
		
		return (Vector<Space>)Environment.getDatabase().query(finder);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Space> findFlatsMatchingCriteria(String criteria)
	{
		UserFlatFinder finder = (UserFlatFinder)HabboHotel.getDataQueryFactory().newQuery("UserFlatFinder");
		finder.criteria = criteria;
		
		return (Vector<Space>)Environment.getDatabase().query(finder);
	}
	
	public boolean createFlat(Space space)
	{
		if (space != null && space.ID == 0)
		{
			// Fill up fields that would turn out to NULL for now
			space.description = "";
			space.usersMax = 25;
			space.wallpaper = 201; // TODO: config
			space.floor = 111; // TODO: config
			space.password = "";
			
			// Store space in the database
			Environment.getDatabase().insert(space);
			
			// Get the ID of the space by looking up the next made space
			return (space.ID > 0);
		}
		
		return false;
	}
	
	public void deleteSpace(Space space)
	{
		if (space != null)
		{
			// Delete Space itself
			Environment.getDatabase().delete(space);
			
			// Delete all it's content
			SpaceContentDeleteHelper helper = (SpaceContentDeleteHelper)HabboHotel.getDataQueryFactory().newQuery("SpaceContentDeleteHelper");
			helper.spaceID = space.ID;
			helper.deleteItems = true;
			helper.deleteFlatControllers = true;
			Environment.getDatabase().execute(helper);
			
			Log.info("Deleted space " + space.ID + " [\"" + space.name + "\"] and all of its content");
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Integer> getFlatControllersForSpace(int spaceID)
	{
		// Create query
		UserFlatControllerHelper helper = (UserFlatControllerHelper)HabboHotel.getDataQueryFactory().newQuery("UserFlatControllerHelper");
		helper.spaceID = spaceID;
		
		return (Vector<Integer>)Environment.getDatabase().query(helper);
	}
	
	public void addFlatControllerForSpace(int spaceID, int userID)
	{
		// Create query
		UserFlatControllerHelper helper = (UserFlatControllerHelper)HabboHotel.getDataQueryFactory().newQuery("UserFlatControllerHelper");
		helper.spaceID = spaceID;
		helper.userID = userID;
		helper.addFlatController = true;
		
		// Execute query
		Environment.getDatabase().execute(helper);
	}
	
	public void removeFlatControllerForSpace(int spaceID, int userID)
	{
		// Create query
		UserFlatControllerHelper helper = (UserFlatControllerHelper)HabboHotel.getDataQueryFactory().newQuery("UserFlatControllerHelper");
		helper.spaceID = spaceID;
		helper.userID = userID;
		helper.addFlatController = false;
		
		// Execute query
		Environment.getDatabase().execute(helper);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Space> getFavoriteFlatListForUser(int userID)
	{
		// Create query
		UserFlatFinder finder = (UserFlatFinder)HabboHotel.getDataQueryFactory().newQuery("UserFlatFinder");
		finder.searchFavorites = true;
		finder.userID = userID;
		
		// Execute query
		return (Vector<Space>)Environment.getDatabase().query(finder);
	}
	
	public void modifyFavoriteFlatListForUser(int userID, int spaceID, boolean addFavorite)
	{
		// Create query
		FavoriteFlatListHelper helper = (FavoriteFlatListHelper)HabboHotel.getDataQueryFactory().newQuery("FavoriteFlatListHelper");
		helper.userID = userID;
		helper.spaceID = spaceID;
		helper.addFavorite = addFavorite;
		
		// Execute query
		Environment.getDatabase().execute(helper);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<SpaceBot> getBotsForSpace(int spaceID)
	{
		// Create query
		SpaceBotLoader loader = (SpaceBotLoader)HabboHotel.getDataQueryFactory().newQuery("SpaceBotLoader");
		loader.spaceID = spaceID;
		
		// Execute query
		return (Vector<SpaceBot>)Environment.getDatabase().query(loader);
	}
	
	/**
	 * Creates a new instance of the Space DataObject implementation class and returns it.
	 */
	public Space newSpace()
	{
		try
		{
			return (Space)m_spaceClass.newInstance();
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns the SpaceModelManager instance holding the SpaceModels.
	 */
	public SpaceModelManager getModels()
	{
		return m_modelManager;
	}
}