package com.suelake.habbo.users;

import com.blunk.Environment;
import com.blunk.Log;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;

/**
 * UserRegister provides methods for managing stored users etc.
 * 
 * @author Nillus
 */
public class UserRegister
{
	private Class<User> m_userClass;
	
	@SuppressWarnings("unchecked")
	public UserRegister()
	{
		User sample = (User)HabboHotel.getDataObjectFactory().newObject("User");
		if (sample != null)
		{
			Class rawClass = sample.getClass();
			m_userClass = rawClass;
		}
	}
	
	/**
	 * Verifies if a given name can be a valid name for a user/pet etc.
	 * 
	 * @param name The name to verify.
	 * @return True if the name is valid, False otherwise.
	 */
	public boolean approveName(String name)
	{
		// FAILproof!
		if (name != null)
		{
			// Atleast 3 characters and not more than 20?
			if (name.length() >= 3 && name.length() <= 20)
			{
				// Does username start with MOD- ?
				if (name.indexOf("MOD-") != 0)
				{
					// We don't want m0d neither...
					if (name.indexOf("M0D-") != 0)
					{
						// Check for characters
						String allowed = HabboHotel.getPropBox().get("user.name.chars", "*");
						if (allowed.equals("*"))
						{
							// Any name can pass!
							return true;
						}
						else
						{
							// Check each character in the name
							char[] nameChars = name.toCharArray();
							for (int i = 0; i < nameChars.length; i++)
							{
								// Is this character allowed?
								if (allowed.indexOf(Character.toLowerCase(nameChars[i])) == -1)
								{
									// Not allowed
									return false;
								}
							}
							
							// Passed all checks!
							return true;
						}
					}
				}
			}
		}
		
		// Bad for whatever reason!
		return false;
	}
	
	public boolean registerUser(User usr, String ip)
	{
		// Verify data
		if (this.approveName(usr.name))
		{
			if (this.getUserInfo(usr.name, false) == null)
			{
				// TODO: verify figure etc
				
				// Log what's going on
				Log.info("Registering new User: " + usr.name);
				
				// Default role
				usr.role = 1;
				
				// Default valueables values
				usr.credits = (short)HabboHotel.getPropBox().getInt("user.default.credits", 0);
				usr.film = (short)HabboHotel.getPropBox().getInt("user.default.film", 0);
				usr.gameTickets = (short)HabboHotel.getPropBox().getInt("user.default.gametickets", 0);
				
				// Default misc values
				usr.messengerMotto = HabboHotel.getPropBox().get("user.default.messengermotto", "null");
				usr.updateLastActivity();
				
				// Default HC values
				usr.hcDaysTotal = 0;
				usr.hcDaysExpired = 0;
				usr.updateLastHcUpdate();
				
				// Default pool figure (none)
				usr.poolFigure = "";
				
				// Store user
				if(Environment.getDatabase().insert(usr))
				{
					HabboHotel.getAccessControl().logRegistration(usr.ID, ip);
					return true;
				}
			}
		}
		
		// Failed
		return false;
	}
	
	public void updateUser(User usr)
	{
		// Update user
		Environment.getDatabase().update(usr);
	}
	
	public User getUserInfo(int userID)
	{
		// TODO: cache?
		
		User usr = this.newUser();
		usr.ID = userID;
		usr.name = null;
		
		if (Environment.getDatabase().load(usr))
		{
			usr.determineBadge();
			return usr;
		}
		else
		{
			return null;
		}
	}
	
	public User getUserInfo(String name, boolean searchClients)
	{
		if (searchClients)
		{
			CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(name);
			if (client != null)
			{
				return client.getUserObject();
			}
		}
		
		User usr = this.newUser();
		usr.ID = 0;
		usr.name = name;
		
		if (Environment.getDatabase().load(usr))
		{
			usr.determineBadge();
			return usr;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Creates a new instance of the User DataObject implementation class and returns it.
	 */
	public User newUser()
	{
		try
		{
			return (User)m_userClass.newInstance();
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