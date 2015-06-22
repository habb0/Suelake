package com.suelake.habbo;

import com.blunk.Environment;
import com.blunk.Log;
import com.blunk.storage.DataObjectFactory;
import com.blunk.storage.DataQueryFactory;
import com.blunk.util.PropertiesBox;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.access.AccessControl;
import com.suelake.habbo.catalogue.Catalogue;
import com.suelake.habbo.communication.ClientManager;
import com.suelake.habbo.economy.VirtualEconomy;
import com.suelake.habbo.items.ItemAdministration;
import com.suelake.habbo.messenger.MessengerService;
import com.suelake.habbo.moderation.ModerationCenter;
import com.suelake.habbo.net.InfoConnectionListener;
import com.suelake.habbo.photos.PhotoService;
import com.suelake.habbo.spaces.SpaceAdministration;
import com.suelake.habbo.spaces.instances.SpaceDirectory;
import com.suelake.habbo.users.UserRegister;
import com.suelake.habbo.util.ChatUtility;

/**
 * Habbo Hotel is a multiuser game created by Sulake Oy, running on their beloved FUSE technology which should be kickass. (sigh)
 * 
 * @author Mike / Nillus
 */
public class HabboHotel
{
	private static long m_startTime;
	private static PropertiesBox m_propBox;
	
	private static DataObjectFactory m_dataObjFactory;
	private static DataQueryFactory m_dataQryFactory;
	
	private static ClientManager m_gameClients;
	private static InfoConnectionListener m_connectionListener;
	
	private static AccessControl m_accessControl;
	private static UserRegister m_userRegister;
	private static SpaceAdministration m_spaceAdmin;
	private static MessengerService m_messengerService;
	private static SpaceDirectory m_spaceDirectory;
	private static ItemAdministration m_itemAdmin;
	private static Catalogue m_catalogue;
	private static PhotoService m_photoService;
	private static ModerationCenter m_moderationCenter;
	private static VirtualEconomy m_virtualEconomy;
	
	/**
	 * Creates a HabboHotel instance in the Blunk environment.
	 * 
	 * @param args arg0 = blunk.properties file. arg1 = habbohotel.properties file.
	 */
	public static void main(String[] args)
	{
		// Blunk environment initialized successfully?
		if ((args != null && args.length >= 2) && Environment.init(args[0]))
		{
			// Print out some fancy text
			System.out.println();
			System.out.println("#########################################");
			System.out.println("##  Habbo Hotel V5 game server         ##");
			System.out.println("##  Copyright (C) 2009                 ##");
			System.out.println("##  Nils [nillus] / Mike [office.boy]  ##");
			System.out.println("##  NILLUS.NET / SCRIPT-O-MATIC.NET    ##");
			System.out.println("#########################################");
			System.out.println();
			
			Log.info("Initializing com.suelake.habbo.HabboHotel...");
			System.out.println();
			
			// Try to initialize habbohotel.properties into a PropertiesBox
			if (initPropBox(args[1]))
			{
				// Try to initialize the configured DataObjects
				if (initDataObjects())
				{
					// Try to initialize the configured DataQueriers
					if (initDataQueries())
					{
						// Prepare AccessControl and load user rights
						m_accessControl = new AccessControl();
						m_accessControl.loadUserRights();
						
						// Prepare UserRegister and loader
						m_userRegister = new UserRegister();
						
						// Prepare ItemAdministration
						m_itemAdmin = new ItemAdministration();
						m_itemAdmin.getDefinitions().loadDefinitions();
						
						// Prepare SpaceAdministration and loader
						m_spaceAdmin = new SpaceAdministration();
						m_spaceAdmin.getModels().loadModels();
						
						// Prepare messenger service
						m_messengerService = new MessengerService();
						
						// Prepare space directory for instance servers
						m_spaceDirectory = new SpaceDirectory(1);
						m_spaceDirectory.addServer(0, 100, "internal");
						
						// Prepare Catalogue
						m_catalogue = new Catalogue();
						m_catalogue.loadPages();
						m_catalogue.loadArticles();
						Log.info("Initialized Catalogue, " + m_catalogue.pageAmount() + " pages, " + m_catalogue.articleAmount() + " articles.");
						
						// Prepare PhotoService
						m_photoService = new PhotoService(m_propBox.getInt("photoservice.port", 30001));
						m_photoService.getServer().setMessageHandlerClass(m_propBox.get("photoservice.msghandler", "NOTDEFINED"));
						
						// Prepare ModerationCenter
						m_moderationCenter = new ModerationCenter();
						
						// Prepare economy
						m_virtualEconomy = new VirtualEconomy();
						
						// Prepare ChatUtility
						ChatUtility.setupEmotes(false);
						
						// Try to initialize the #info listener and client manager
						if (initClientManager())
						{
							// Start servers
							m_gameClients.startMonitor();
							m_connectionListener.start();
							m_photoService.getServer().start();
							
							// Install shutdown thread
							Runtime.getRuntime().addShutdownHook(new ShutdownThread());
							
							// Done!
							System.out.println();
							Log.info("Initialized com.suelake.habbo.HabboHotel.");
							System.out.println();
							
							// Set starttime!
							m_startTime = TimeHelper.getTime();
							return;
						}
					}
				}
			}
		}
		
		Log.error("Could not initialize Blunk environment.");
		Log.error("Could not initialize com.suelake.HabboHotel.");
	}
	
	private static boolean initPropBox(String propertiesFile)
	{
		Log.info("Locating .properties file for Habbo Hotel...");
		m_propBox = new PropertiesBox();
		if (!m_propBox.load(propertiesFile))
		{
			Log.error("Could not load Habbo Hotel .properties file " + propertiesFile);
			return false;
		}
		Log.info("Initialized properties for Habbo Hotel, " + m_propBox.size() + " properties loaded.");
		
		return true;
	}
	
	private static boolean initDataObjects()
	{
		int maxObjects = m_propBox.getInt("db.objects.max", 25);
		String dbImplName = Environment.getPropBox().get("db.impl", "NOTDEFINED");
		m_dataObjFactory = new DataObjectFactory(dbImplName, maxObjects);
		
		Log.info("Registering DataObject classes...");
		
		int classID = 0;
		int registeredCounter = 0;
		String objName = null;
		
		while ((objName = m_propBox.get("db.object[" + classID++ + "]")) != null)
		{
			if (m_dataObjFactory.registerObjectClass(objName))
			{
				registeredCounter++; // OK!
			}
		}
		
		Log.info("Registered " + registeredCounter + " DataObject classes.");
		
		return true;
	}
	
	private static boolean initDataQueries()
	{
		int maxQueries = m_propBox.getInt("db.queries.max", 25);
		String dbImplName = Environment.getPropBox().get("db.impl", "NOTDEFINED");
		m_dataQryFactory = new DataQueryFactory(dbImplName, maxQueries);
		
		Log.info("Registering DataQuery classes...");
		
		int classID = 0;
		int registeredCounter = 0;
		String qryName = null;
		
		while ((qryName = m_propBox.get("db.query[" + classID++ + "]")) != null)
		{
			if (m_dataQryFactory.registerQueryClass(qryName))
			{
				registeredCounter++; // OK!
			}
		}
		
		Log.info("Registered " + registeredCounter + " DataQuery classes.");
		
		return true;
	}
	
	private static boolean initClientManager()
	{
		// Create ClientManager
		int maxClients = m_propBox.getInt("client.max", 255);
		m_gameClients = new ClientManager(maxClients);
		
		// Create listener
		int listenerPort = m_propBox.getInt("client.infoport", 30000);
		m_connectionListener = new InfoConnectionListener(listenerPort, m_gameClients);
		
		return true;
	}
	
	public static long getStartTime()
	{
		return m_startTime;
	}
	
	public static PropertiesBox getPropBox()
	{
		return m_propBox;
	}
	
	public static DataObjectFactory getDataObjectFactory()
	{
		return m_dataObjFactory;
	}
	
	public static DataQueryFactory getDataQueryFactory()
	{
		return m_dataQryFactory;
	}
	
	public static InfoConnectionListener getGameConnectionListener()
	{
		return m_connectionListener;
	}
	
	public static ClientManager getGameClients()
	{
		return m_gameClients;
	}
	
	public static AccessControl getAccessControl()
	{
		return m_accessControl;
	}
	
	public static UserRegister getUserRegister()
	{
		return m_userRegister;
	}
	
	public static SpaceAdministration getSpaceAdmin()
	{
		return m_spaceAdmin;
	}
	
	public static MessengerService getMessengerService()
	{
		return m_messengerService;
	}
	
	public static SpaceDirectory getSpaceDirectory()
	{
		return m_spaceDirectory;
	}
	
	public static ItemAdministration getItemAdmin()
	{
		return m_itemAdmin;
	}
	
	public static Catalogue getCatalogue()
	{
		return m_catalogue;
	}
	
	public static PhotoService getPhotoService()
	{
		return m_photoService;
	}
	
	public static ModerationCenter getModerationCenter()
	{
		return m_moderationCenter;
	}
	
	public static VirtualEconomy getEconomy()
	{
			return m_virtualEconomy;
	}
}
