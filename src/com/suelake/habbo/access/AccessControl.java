package com.suelake.habbo.access;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.util.ArrayUtil;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.users.UserRole;

/**
 * AccessControl provides methods for logging user account access, checking if users have certain privileges etc.
 * 
 * @author Nillus
 */
public class AccessControl
{
	private Class<UserAccessEntry> m_userAccessEntryClass;
	private String[][] m_userRights;
	private String m_messageOfTheDay;
	
	@SuppressWarnings("unchecked")
	public AccessControl()
	{
		UserAccessEntry sample = (UserAccessEntry)HabboHotel.getDataObjectFactory().newObject("UserAccessEntry");
		if (sample != null)
		{
			Class rawClass = sample.getClass();
			m_userAccessEntryClass = rawClass;
		}
		
		m_userRights = new String[0][0];
		m_messageOfTheDay = null;
	}
	
	@SuppressWarnings("unchecked")
	public void loadUserRights()
	{
		// Create blank array
		m_userRights = new String[UserRole.MAX_USER_ROLE + 1][0];
		
		// Create query
		UserRightLoader loader = (UserRightLoader)HabboHotel.getDataQueryFactory().newQuery("UserRightLoader");
		if (loader != null)
		{
			// Cycle all roles
			for (byte roleID = 0; roleID <= UserRole.MAX_USER_ROLE; roleID++)
			{
				// Prepare the loader 
				loader.roleID = roleID;
				
				// Query to Vector
				Vector<String> rights = (Vector<String>)Environment.getDatabase().query(loader);
				
				// Parse vector to string array for easier reading later
				m_userRights[roleID] = ArrayUtil.convertStringVectorToArray(rights);
			}
		}
	}
	
	public String[] getUserRightsForRole(byte roleID)
	{
		// Role in range?
		if(roleID <= UserRole.MAX_USER_ROLE)
		{
			return m_userRights[roleID];
		}
		
		// No rights should be appropriate here
		return new String[0];
	}
	
	public boolean roleHasUserRight(byte roleID, String right)
	{
		// Role in range?
		if(roleID <= UserRole.SYSTEM_ADMINISTRATOR)
		{
			// Cycle all rights for this role
			for(String userRight : m_userRights[roleID])
			{
				// Match?
				if(userRight.equals(right))
				{
					// Found!
					return true;
				}
			}
		}
		
		// Nope!
		return false;
	}
	

	public byte getMinimumRoleForUserRight(String right)
	{
		// Cycle all roles
		for (byte roleID = 0; roleID <= UserRole.MAX_USER_ROLE; roleID++)
		{
			if(this.roleHasUserRight(roleID, right))
			{
				return roleID;
			}
		}
		
		// Ensure this right is never valid for a User
		return UserRole.MAX_USER_ROLE + 1;
	}
	
	public void logLogin(UserAccessEntry entry)
	{
		if (entry != null)
		{
			entry.login = TimeHelper.getDateTime();
			Environment.getDatabase().insert(entry);
		}
	}
	
	public void logLogout(UserAccessEntry entry)
	{
		if (entry != null && entry.ID > 0)
		{
			entry.logout = TimeHelper.getDateTime();
			Environment.getDatabase().update(entry);
		}
	}
	
	public UserAccessEntry getLatestAccessEntry(int userID)
	{
		UserAccessEntry entry = this.newUserAccessEntry();
		entry.ID = 0;
		entry.userID = userID;
		
		if (Environment.getDatabase().load(entry))
		{
			return entry;
		}
		
		return null;
	}
	
	public void logRegistration(int userID, String ip)
	{
		UserAccessEntry entry = this.newUserAccessEntry();
		entry.userID = userID;
		entry.ip = ip;
		entry.login = TimeHelper.getDateTime();
		entry.isRegistration = true;
		
		Environment.getDatabase().insert(entry);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<String> getRegisteredUsers(String ip)
	{
		UserRegistrationFinder finder = (UserRegistrationFinder)HabboHotel.getDataQueryFactory().newQuery("UserRegistrationFinder");
		finder.ip = ip;
		
		return (Vector<String>)Environment.getDatabase().query(finder);
	}
	
	public String getIpIsBanned(String ip)
	{
		return null;
	}
	
	public String getMessageOfTheDay()
	{
		return m_messageOfTheDay;
	}
	
	public void setMessageOfTheDay(String motd)
	{
		m_messageOfTheDay = motd;
	}
	
	/**
	 * Creates a new instance of the UserAccessEntry DataObject implementation class and returns it.
	 */
	public UserAccessEntry newUserAccessEntry()
	{
		try
		{
			return m_userAccessEntryClass.newInstance();
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
}
