package com.suelake.habbo.items;


import com.blunk.storage.DataObject;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * An Item is a furniture item that can be either placed in a public space (PassiveObject), in a user flat (ActiveObject) or on the wall of a user flat (WallItem).
 * @author Nillus
 *
 */
public abstract class Item implements DataObject, SerializableObject
{
	/**
	 * The ID (number) of this item, unique in the Database.
	 */
	public int ID;
	
	/**
	 * The database ID of the User that owns this item.
	 */
	public int ownerID;
	/**
	 * The database ID of the Space this item is in.
	 */
	public int spaceID;
	
	/**
	 * The ItemDefinition object holding information about this item type.
	 */
	public ItemDefinition definition;
	
	/**
	 * The database ID of the other teleporter Item if this Item is a teleporter.
	 */
	public int teleporterID;
	/**
	 * A string holding custom data to the item, such as status etc.
	 */
	public String customData;
	/**
	 * A string holding item data to the Item. ('item' = wallitem) This can be data like text content of a post.it note, but also the display text of a photo.
	 */
	public String itemData;
	
	/**
	 * The X position of this item on the map.
	 */
    public short X;
	/**
	 * The Y position of this item on the map.
	 */
	public short Y;
    /**
     * The height this item is located at as floating point value.
     */
    public float Z;
    /**
     * The rotation of this item.
     */
    public byte rotation;
    
	/**
	 * The position of this WallItem on the wall of the Space as a String.
	 */
	public String wallPosition;
	
	public boolean setDefinition(int definitionID)
	{
		this.definition = HabboHotel.getItemAdmin().getDefinitions().getDefinition(definitionID);
		return (this.definition != null);
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		if(this.definition.behaviour.onFloor)
		{
			if(this.definition.behaviour.isPassiveObject)
			{
				msg.appendNewArgument(Integer.toString(this.ID));
				msg.appendArgument(this.definition.sprite);
				msg.appendArgument(Integer.toString(this.X));
				msg.appendArgument(Integer.toString(this.Y));
				msg.appendArgument(Integer.toString((int)this.Z));
				msg.appendArgument(Byte.toString(this.rotation));
			}
			else
			{
				msg.appendNewArgument(Integer.toString(this.ID));
		    	msg.appendArgument(this.definition.sprite, ',');
		    	msg.appendArgument(Integer.toString(this.X));
		    	msg.appendArgument(Integer.toString(this.Y));
		    	msg.appendArgument(Byte.toString(this.definition.length));
		    	msg.appendArgument(Byte.toString(this.definition.width));
		    	msg.appendArgument(Byte.toString(this.rotation));
		    	msg.appendArgument(Float.toString(this.Z));
		    	msg.appendArgument(this.definition.color);
		    	
		    	msg.appendArgument(this.definition.name, '/');
		    	msg.appendArgument(this.definition.description, '/');
		    	
		    	// Teleporter?
		    	if(this.teleporterID > 0)
		    	{
		    		msg.appendArgument("extr=", '/');
		    		msg.appendArgument(Integer.toString(this.teleporterID), '/');
		    	}
		    	
		    	// Custom data?
		    	if(this.customData != null)
		    	{
		    		// Append custom data class
		    		msg.appendArgument(this.definition.customDataClass, '/');
		    		msg.appendArgument(this.customData, '/');
		    	}
			}
		}
		else if(this.definition.behaviour.onWall)
		{
			msg.appendNewArgument(Integer.toString(this.ID));
			msg.appendTabArgument(this.definition.sprite);
			msg.appendTabArgument(" ");
			msg.appendTabArgument(this.wallPosition);
			if(this.customData != null)
			{
				msg.appendTabArgument(this.customData);
			}
		}
	}
	
	public void serialize(ServerMessage msg, int stripSlotID)
	{	
		//  |stripID||S|id|class|name|custom|x|y|FFFFFF/  |stripID||I|id|class|name|props|custom
		msg.appendArgument(Integer.toString(this.ID), '|');
		msg.appendArgument("blunk", '|');
		if(this.definition.behaviour.STUFF)
		{
			msg.appendArgument("S", '|');
		}
		else if(this.definition.behaviour.ITEM)
		{
			msg.appendArgument("I", '|');
		}
		msg.appendArgument(Integer.toString(stripSlotID), '|');
		msg.appendArgument(this.definition.sprite, '|');
		msg.appendArgument(this.definition.name, '|');
		if(this.definition.behaviour.STUFF)
		{
			msg.appendArgument(this.customData, '|');
			msg.appendArgument(Integer.toString(this.definition.length), '|');
			msg.appendArgument(Integer.toString(this.definition.width), '|');
			msg.appendArgument(this.definition.color, '|');
		}
		else if(this.definition.behaviour.ITEM)
		{
			msg.appendArgument(this.customData, '|');
			msg.appendArgument(this.customData, '|');
		}
		
		msg.append("/"); // Delimit items!
	}
	
	public long getCacheKey()
	{
		return this.ID;
	}
}
