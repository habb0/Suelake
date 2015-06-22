package com.suelake.habbo.spaces;


import com.blunk.storage.DataObject;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * Space is the information on a public space or user flat, such as details of the space etc.
 * 
 * @author Nillus
 */
public abstract class Space implements DataObject, SerializableObject
{
	/**
	 * The database ID of this space. Unique.
	 */
	public int ID;
	/**
	 * The database ID of the User that owns this space.
	 */
	public int ownerID;
	
	/**
	 * The name of this space, displayed to users of the service.
	 */
	public String name;
	/**
	 * The description of this space. If this space is a userflat, it holds a small description on
	 * this user flat. If this space is a public space, it holds the name of the castfile.
	 */
	public String description;
	/**
	 * The model type of this space, each model has it's own heightmap, door position etc.
	 */
	public String model;
	/**
	 * The current amount of Users in this space.
	 */
	public short usersNow;
	/**
	 * The maximum amount of simultaneous Users this space can hold. Some Users can override this
	 * limit.
	 */
	public short usersMax;
	
	/**
	 * User flat only. The name of the User that owns this space.
	 */
	public String owner;
	/**
	 * User flat only. If false, only Users with special privileges can see the name of the User
	 * that owns this user flat.
	 */
	public boolean showOwner;
	/**
	 * User flat only. If true, all Users inside have the right to drop items and to move items
	 * around.
	 */
	public boolean superUsers;
	public String accessType;
	/***
	 * User flat only. If accessType = 'password', the password that Users must give when entering
	 * this user flat is stored in this field.
	 */
	public String password;
	/**
	 * User flat only. The wallpaper FLATPROPERTY of this user flat.
	 */
	public short wallpaper;
	/**
	 * User flat only. The floor FLATPROPERTY of this user flat.
	 */
	public short floor;
	
	public boolean isUserFlat()
	{
		return (this.ownerID > 0);
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		if(this.isUserFlat())
		{
			msg.appendNewArgument(Integer.toString(this.ID));
			msg.appendTabArgument(this.name);
			msg.appendTabArgument(this.owner);
			msg.appendTabArgument(this.accessType);
			msg.appendTabArgument("127.0.0.1"); // Flat server IP
			msg.appendTabArgument(Integer.toString(this.usersNow));
			msg.appendTabArgument("x"); // Papa was a rolling stone. See? Whatever
			msg.appendTabArgument(this.description);
		}
		else
		{
			msg.appendNewArgument(Integer.toString(this.ID));
			msg.appendTabArgument(Integer.toString(this.ID)); // Space server port
			msg.appendTabArgument(this.name);
			msg.appendTabArgument(Short.toString(this.usersNow));
			msg.appendTabArgument(Short.toString(this.usersMax));
			msg.appendTabArgument(this.name);
			msg.appendTabArgument(Short.toString(this.usersNow));
			msg.appendTabArgument(Short.toString(this.usersMax));
			msg.append("\t");
		}
	}
	
	@Override
	public long getCacheKey()
	{
		return this.ID;
	}
}
