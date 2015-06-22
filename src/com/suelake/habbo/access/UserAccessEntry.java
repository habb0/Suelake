package com.suelake.habbo.access;

import java.util.Date;

import com.blunk.storage.DataObject;

/**
 * Represents a accesslog entry of a user, holding information about the computer that was used
 * during the access etc.
 * 
 * @author Nillus
 */
public abstract class UserAccessEntry implements DataObject
{
	/**
	 * The ID of this access entry. This is an unique number.
	 */
	public int ID;
	/**
	 * The ID of the user this access entry is for.
	 */
	public int userID;
	/**
	 * The IP address string (dots 'n digits) that was used during the access.
	 */
	public String ip;
	/**
	 * The java.util.Date representing the moment this access started. (login)
	 */
	public Date login;
	/**
	 * The java.util.Date representing the moment this access stopped. (logout)
	 */
	public Date logout;
	/**
	 * Whether this was a registration instead of a login.
	 */
	public boolean isRegistration;
}
