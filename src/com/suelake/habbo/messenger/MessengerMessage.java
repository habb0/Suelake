package com.suelake.habbo.messenger;

import java.util.Date;

import com.blunk.storage.DataObject;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * MessengerMessage is is a textmessage sent on the ingame messenger ('Console'), from one user to another. Users can also receive campaign and staff messages.
 * Messages are stored in database even when they are marked as 'read', so moderation can include checking out those messages y0.
 * MessengerMessage is storeable in a Database.
 * @author Nillus
 *
 */
public abstract class MessengerMessage implements DataObject, SerializableObject
{
	/**
	 * The unique ID of this messenger message. Each individual message has it's own ID, which is unique in the database.
	 */
	public int ID;
	/**
	 * The ID of the user that sent this message.
	 */
	public int senderID;
	/**
	 * The ID of the user that this message is sent to.
	 */
	public int receiverID;
	/**
	 * The java.util.Date representing the moment this message was sent.
	 */
	public Date timestamp;
	/**
	 * The actual text in this message. ('body')
	 */
	public String text;
	/**
	 * True if the receiver has read this message and marked it as 'read', False otherwise. (doh) 
	 */
	public boolean read;
	/**
	 * The String representing the figure of the sender for displaying the head of the user next to the message in client. This is retrieved from a separate location than the message location.
	 */
	public String senderFigure;
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendNewArgument(Integer.toString(this.ID));
		msg.appendNewArgument(Integer.toString(this.senderID));
		msg.appendNewArgument("");
		msg.appendNewArgument(TimeHelper.formatDateTime(this.timestamp));
		msg.appendNewArgument(this.text);
		msg.appendNewArgument(this.senderFigure);
		msg.appendNewArgument("");
	}
	
	@Override
	public long getCacheKey()
	{
		return this.ID;
	}
}
