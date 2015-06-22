package com.suelake.habbo.items;

/**
 * Represents a 'template' of a furniture item.
 * @author Nillus
 *
 */
public class ItemDefinition
{
	/**
	 * The ID of this definition.
	 */
	public int ID;
	/**
	 * The sprite name of this item.
	 */
	public String sprite;
	/**
	 * The color string of this item. (hex colors)
	 */
	public String color;
	/**
	 * The length of this item in tiles.
	 */
	public byte length;
	/**
	 * The width of this item in tiles.
	 */
	public byte width;
	/**
	 * The height the 'top' of this item is located at. Affects the height units and objects are located on when they are 'on top' of this item. 
	 */
	public float heightOffset;
	
	/**
	 * The classname of the 'custom data class' of this item. "NULL" if no class.
	 */
	public String customDataClass;
	/**
	 * The ItemBehaviour object holding the flags for the behaviour of this item.
	 */
	public ItemBehaviour behaviour;
	
	/**
	 * The ingame name of this item.
	 */
	public String name;
	/**
	 * The ingame description of this item.
	 */
	public String description;
}
