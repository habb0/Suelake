package com.suelake.habbo.photos;

import com.blunk.Environment;
import com.blunk.Log;
import com.blunk.mus.MultiUserServer;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.items.Item;

/**
 * PhotoService provides methods for saving and retrieving 'photos' from the Database.\r
 * Photos are binary images made by Users with the Camera item.
 * 
 * @author Nillus
 */
public class PhotoService
{
	@SuppressWarnings("unchecked")
	private Class m_photoClass;
	private MultiUserServer m_server;
	
	public PhotoService(int port)
	{
		Photo sample = (Photo)HabboHotel.getDataObjectFactory().newObject("Photo");
		if (sample != null)
		{
			m_photoClass = sample.getClass();
		}
		
		// Create server
		m_server = new MultiUserServer(port);
	}
	
	/**
	 * Retrieves a Photo DataObject with a given photo ID from the Database.
	 * 
	 * @param photoID The database ID of the Photo to retrieve.
	 * @return The Photo DataObject if found and loaded, NULL otherwise.
	 */
	public Photo getPhoto(int photoID)
	{
		Photo photo = this.newPhoto();
		photo.ID = photoID;
		
		// Complete with data from Database
		if (Environment.getDatabase().load(photo))
		{
			return photo;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Attempts to store a new Photo DataObject in the Database.
	 * 
	 * @param photo The Photo DataObject to store.
	 * @return True if storage succeeded, False otherwise.
	 */
	public boolean storePhoto(Photo photo)
	{
		// Store
		Log.info("PhotoService: storing new Photo in Database (" + photo.image.length + " bytes)");
		Environment.getDatabase().insert(photo);
		
		// Stored?
		return (photo.ID > 0);
	}
	
	/**
	 * Deletes a Photo DataObject with a given photo ID from the Database.
	 * 
	 * @param photoID The database ID of the Photo DataObject to delete.
	 */
	public void deletePhoto(int photoID)
	{
		Photo helper = this.newPhoto();
		helper.ID = photoID;
		Environment.getDatabase().delete(helper);
	}
	
	public void handlePhotoCreation(int userID, int photoID, String time, String txt)
	{
		// Filter # from 'photo text' to prevent scripting
		txt = txt.replace('#', '?');
		
		// Construct DataObject
		Item item = HabboHotel.getItemAdmin().newItem();
		item.ownerID = userID;
		item.definition = HabboHotel.getItemAdmin().getDefinitions().getPhotoDefinition();
		item.customData = Integer.toString(photoID);
		item.itemData = time + "\r" + txt;
		
		// Attempt to insert DataObject
		boolean itemCreated = (item.definition != null && HabboHotel.getItemAdmin().storeItem(item));
		
		// Notify live client
		CommunicationHandler client = HabboHotel.getGameClients().getClientOfUser(userID);
		if (client != null)
		{
			if (itemCreated)
			{
				// Deduct film
				client.getUserObject().film--;
				client.sendFilm();
				
				// Refresh item strip
				client.getItemInventory().addItem(item);
				client.getItemInventory().sendStrip("last");
				
				// Update user object
				HabboHotel.getUserRegister().updateUser(client.getUserObject());
			}
			else
			{
				client.systemMsg("Photo service error!\rYour photo data was stored in the Database under ID " + photoID + ", but the ItemAdmin was unable to create a Item DataObject!\rPlease contact administrator with this information.");
			}
		}
	}
	
	/**
	 * Creates a new instance of the Photo DataObject implementation class and returns it.
	 */
	public Photo newPhoto()
	{
		try
		{
			return (Photo)m_photoClass.newInstance();
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
	
	/**
	 * Returns the MultiUserServer instance.
	 */
	public MultiUserServer getServer()
	{
		return m_server;
	}
}
