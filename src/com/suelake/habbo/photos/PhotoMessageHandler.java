package com.suelake.habbo.photos;

import java.text.SimpleDateFormat;

import com.blunk.mus.MusClient;
import com.blunk.mus.MusMessage;
import com.blunk.mus.MusMessageHandler;
import com.blunk.mus.MusPropList;
import com.blunk.mus.MusTypes;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.users.User;

public class PhotoMessageHandler implements MusMessageHandler
{
	private String m_photoText;
	
	@Override
	public void cleanup()
	{
		m_photoText = null;
	}
	
	@Override
	public void handleIncomingMessage(MusClient client, MusMessage request)
	{
		MusMessage response = new MusMessage();
		if (request.subject.equals("Logon"))
		{
			// Default system logon
			response.subject = "Logon";
			response.contentType = MusTypes.String;
			response.contentString = "Blunk: multi user server framework";
			client.sendMessage(response);
			
			// OHAI THAR
			response = new MusMessage();
			response.subject = "HELLO";
			response.contentType = MusTypes.String;
			response.contentString = "";
			client.sendMessage(response);
		}
		else if (request.subject.equals("LOGIN"))
		{
			// Parse credentials
			String[] credentials = request.contentString.split(" ", 2);
			
			// Get user object
			User userObject = HabboHotel.getUserRegister().getUserInfo(credentials[0], true);
			
			// Validate credentials
			if (userObject != null && userObject.password.equals(credentials[1]))
			{
				// Logged in OK!
				client.setUserID(userObject.ID);
			}
			else
			{
				// Bad login
				client.stop();
			}
		}
		else if (request.subject.equals("PHOTOTXT"))
		{
			// Requires LOGIN
			if (client.getUserID() > 0)
			{
				// Get the 'photo text'
				m_photoText = request.contentString.substring(1);
			}
		}
		else if (request.subject.equals("BINDATA"))
		{
			// Requires LOGIN and PHOTOTXT
			if (client.getUserID() > 0 && m_photoText != null)
			{
				// Create new Photo DataObject
				Photo entity = HabboHotel.getPhotoService().newPhoto();
				if (entity != null)
				{
					// Setup Photo DataObject
					entity.time = request.contentPropList.getPropAsString("time");
					entity.cs = request.contentPropList.getPropAsInt("cs");
					entity.image = request.contentPropList.getPropAsBytes("image");
					
					// Let's keep it real
					if (entity.time.length() < 3 || entity.time.length() > 20)
					{
						entity.time = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(TimeHelper.getDateTime());
					}
					
					// Save photo
					if (HabboHotel.getPhotoService().storePhoto(entity))
					{
						// Photo saved OK!
						response.subject = "BINDATA_SAVED";
						response.contentType = MusTypes.String;
						response.contentString = Integer.toString(entity.ID);
						client.sendMessage(response);
						
						// Deduct film, create Item and deliver to user
						HabboHotel.getPhotoService().handlePhotoCreation(client.getUserID(), entity.ID, entity.time, m_photoText);
					}
				}
			}
		}
		else if (request.subject.equals("GETBINDATA"))
		{
			// Parse photo ID 
			int photoID = Integer.parseInt(request.contentString.split(" ")[0]);
			
			// Get Photo DataObject from Database
			Photo entity = HabboHotel.getPhotoService().getPhoto(photoID);
			if (entity != null)
			{
				// Build response
				response.subject = "BINARYDATA";
				response.contentType = MusTypes.PropList;
				response.contentPropList = new MusPropList(3);
				response.contentPropList.setPropAsBytes("image", MusTypes.Media, entity.image);
				response.contentPropList.setPropAsString("time", entity.time);
				response.contentPropList.setPropAsInt("cs", entity.cs);
				client.sendMessage(response);
			}
		}
		else
		{
			// ....
		}
	}
}
