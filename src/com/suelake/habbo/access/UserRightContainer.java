package com.suelake.habbo.access;

/**
 * UserRightContainer contains the user rights for a given UserRole. This includes rights for underlying roles.
 * 
 * @author Nillus
 *
 */
public class UserRightContainer
{
	/**
	 * The role ID for this UserRightContainer.
	 */
	public final byte roleID;
	/***
	 * String array holding the rights for this User role.
	 */
	private String[] rights;
	
	/**
	 * Constructs an empty UserRightContainer for a given User role.
	 * @param roleID The User role ID.
	 */
	public UserRightContainer(byte roleID)
	{
		this.roleID = roleID;
	}
	
	/**
	 * Checks if a given User right exists in this UserRightContainer.
	 * @param userRight The User right to check.
	 * @return True if it exists, false otherwise.
	 */
	public boolean containsRight(String userRight)
	{
		for(String right : this.rights)
		{
			if(right.equals(userRight))
			{
				return true;
			}
		}
		
		// Nuh-uh!
		return false;
	}
}
