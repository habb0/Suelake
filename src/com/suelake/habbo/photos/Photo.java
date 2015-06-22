package com.suelake.habbo.photos;

import com.blunk.storage.DataObject;

/**
 * Photo represents a binary image shot by a User with the Camera item.
 * @author Nillus
 *
 */
public abstract class Photo implements DataObject
{
	/**
	 * The ID of this Photo. Unique in the photos sector of the Database.
	 */
	public int ID;
	/**
	 * A string holding a client-sent timestamp of when the Photo was shot.
	 */
	public String time;
	/**
	 * A random value that is still unknown these days.
	 */
	public int cs;
	/**
	 * A byte array holding the binary data of the image.
	 */
	public byte[] image;
}
