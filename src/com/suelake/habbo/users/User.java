package com.suelake.habbo.users;

import java.util.Date;

import com.blunk.storage.DataObject;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * User is a DataObject that holds data about a user, such as ingame details, personal details and more. User is storeable into the Database with an implementation.
 * 
 * @author Nillus
 */
public class User implements DataObject, SerializableObject
{
	/**
	 * The ID of this user. User IDs are unique.
	 */
	public int ID;
	/**
	 * The name of this User. User names are unique.
	 */
	public String name;
	/**
	 * The password of this User. The User uses this, in combination with name, to login to the service.
	 */
	public String password;
	
	/**
	 * The personal e-mail address string of this User, given at register.
	 */
	public String email;
	/**
	 * The personal date of birth string of this User, given at register.
	 */
	public String dateOfBirth;
	/**
	 * The ID of the country of this User. (eg, London)
	 */
	public short countryID;
	/**
	 * The ID of the region the country is in (eg, UK, Europe, Asia) of this User.
	 */
	public short regionID;
	/**
	 * The phone number string of this User.
	 */
	public String phoneNumber;
	/**
	 * The Date representing this User's signup.
	 */
	public Date registered;
	
	/**
	 * The role ID of this User, Users with a higher role have more privileges than other Users and can overpower other Users in certain contexts.
	 */
	public byte role;
	
	/*
	 * FIGURE (OLD FORMAT)
	 hd=head
	 bd=body
	 fc=face
	 ey=eye
	 ch=chest
	 ls=left sleeve
	 rs=right sleeve
	 rh=right arm
	 lh=left arm
	 lg=leg
	 ft=feet
	 sd=shadow
	*/
	/**
	 * The motto (customData) of this User's avatar, displayed to other users in the service.
	 */
	public String motto;
	/**
	 * The figure string (appearance) of this User's avatar.
	 */
	public String figure;
	/**
	 * The gender of this User's avatar. This can be either 'M' for male and 'F' for female.
	 */
	public char sex;
	
	/**
	 * The amount of coins ('credits') this User has. Credits are used to purchase all kinds of things in and around the service.
	 */
	public short credits;
	/**
	 * The amount of film this User has. Films are used with the camera item to make pictures.
	 */
	public short film;
	/**
	 * The amount of game tickets this User as. Game tickets are used to purchase rides and more in the service.
	 */
	public short gameTickets;
	
	/**
	 * Holds the 'pool figure' of this User. Displayed in spaces with a swimming pool. Null if no pool clothes are selected.
	 */
	public String poolFigure;
	
	/**
	 * The badge this User currently has, this is displayed to other Users while in room and depends on user role and subscriptions etc.
	 */
	public String badge;
	
	/**
	 * The 'persistent message' of this user, displayed in the messenger of this User.
	 */
	public String messengerMotto;
	/**
	 * The java.util.Date representing the moment this user had last activity on the server.
	 */
	public Date lastActivity;
	
	/**
	 * The total amount of days this user has a subscription since. This number will never get smaller, everytime Users extend their subscription the added days get added to this value.
	 */
	public short hcDaysTotal;
	/**
	 * The total amount of expired HC subscription days. This number will never get smaller.
	 */
	public short hcDaysExpired;
	/**
	 * Date object representing the last date + time HC subscription was calculated & worked out.
	 */
	public Date hcLastUpdate;
	
	public void updateLastActivity()
	{
		this.lastActivity = TimeHelper.getDateTime();
	}
	
	public void updateLastHcUpdate()
	{
		this.hcLastUpdate = TimeHelper.getDateTime();
	}
	
	public int getPendingHcDays()
	{
		int days = this.hcDaysTotal - this.hcDaysExpired;
		return (days > 0) ? days : 0;
	}
	
	public boolean isHC()
	{
		return (this.getPendingHcDays() > 0);
	}
	
	/**
	 * Determines the Users badge status by checking HC subscription and role. 'Highest badge' is set, or NULL when there is no badge.
	 */
	public void determineBadge()
	{
		/*
		 * Roles
		 * 1 = Normal
		 * 2 = Silver Hobba
		 * 3 = Gold Hobba
		 * 4 = Moderator
		 * 5 = Administrator
		 * 6 = etc
		 */
		
		if(this.role == UserRole.NORMAL && this.isHC())
		{
			this.badge = "H";
		}
		else if(this.role == UserRole.SILVER)
		{
			this.badge = "1"; // Silver Hobba badge
		}
		else if(this.role == UserRole.GOLD)
		{
			this.badge = "2"; // Gold Hobba badge
		}
		else if(this.role > UserRole.GOLD)
		{
			this.badge = "A"; // Staff badge
		}
		else
		{
			this.badge = null; // No badge
		}
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendKVArgument("name", this.name);
		msg.appendKVArgument("email", this.email);
		msg.appendKVArgument("birthday", this.dateOfBirth);
		msg.appendKVArgument("phoneNumber", this.phoneNumber);
		msg.appendKVArgument("region", Short.toString(this.regionID));
		msg.appendKVArgument("country", Short.toString(this.countryID));
		msg.appendKVArgument("directMail", "0"); // No spam please
		msg.appendKVArgument("had_read_agreement", "1"); // Ofcourse
		msg.appendKVArgument("customData", this.motto);
		msg.appendKVArgument("sex", Character.toString(this.sex));
		msg.appendKVArgument("figure", this.figure);
		
	}
	
	/**
	 * Determines if this UserObjects role has access to a given User right.
	 * @param right The right to check.
	 * @see com.suelake.habbo.access.AccessControl
	 */
	public boolean hasRight(String right)
	{
		return HabboHotel.getAccessControl().roleHasUserRight(this.role, right);
	}
	
	public boolean calculateHC(boolean force)
	{
		// Only do this when user is HC and update is forced OR required
		if (force || (this.isHC()))
		{
			// Calculate days passed since last update
			long daysPassed = TimeHelper.calculateDaysElapsed(this.hcLastUpdate);
			if(force && daysPassed == 0) daysPassed = 1;
			if(daysPassed > 0)
			{
				// Subtract each passed day individually
				while (daysPassed > 0 && (this.hcDaysExpired < this.hcDaysTotal))
				{
					daysPassed--;
					this.hcDaysExpired++;
					
					// At the start of a new subscription month?
					if ((this.hcDaysExpired - 31) % 30 == 0)
					{
						// Calculate current month
						int month = (this.hcDaysExpired / 30) + 1;
						
						// Give gift
						HabboHotel.getCatalogue().shipClubGift(this.ID, month);
					}
				}
				
				// All subscription days used?
				if ((this.hcDaysTotal - this.hcDaysExpired) <= 0)
				{
					this.figure = HabboHotel.getPropBox().get("user.default.figure", "1000118001270012900121001");
				}
			}
			
			// Set last updated moment to NOW
			this.hcLastUpdate = TimeHelper.getDateTime();
			return true;
		}
		
		return false;
	}

	@Override
	public long getCacheKey()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
