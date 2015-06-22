package com.suelake.habbo.spaces.instances;

import java.util.Vector;

import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.spaces.pathfinding.PathfinderNode;
import com.suelake.habbo.spaces.pathfinding.RotationCalculator;
import com.suelake.habbo.users.User;

/**
 * Represents a user or bot in a space instance.
 * 
 * @author Nillus
 */
public class SpaceUser implements SerializableObject
{
	private static final int MAX_SIMULTANEOUS_STATUSES = 5;
	
	/**
	 * The User DataObject holding information about this user.
	 */
	private User m_userObject;
	
	/**
	 * The CommunicationHandler connecting this client with the space instance. Null if bot.
	 */
	private CommunicationHandler m_comm;
	
	/**
	 * The current X position of this space user in the space.
	 */
	public short X;
	/**
	 * The current Y position of this space user in the space.
	 */
	public short Y;
	/**
	 * The current height position of this space user in the space.
	 */
	public float Z;
	
	/**
	 * The current rotation (direction) of this space user's head.
	 */
	public byte headRotation;
	/**
	 * The current rotation (direction) of this space user's body.
	 */
	public byte bodyRotation;
	
	/**
	 * True if this user currently can not select a new goal tile for moving.
	 */
	public boolean moveLock = false;
	/**
	 * The X position of the goal tile this user walks to. -1 if not walking.
	 */
	public short goalX;
	/**
	 * The Y position of the goal tile this user walks to.
	 */
	public short goalY;
	public Vector<PathfinderNode> path;
	/**
	 * True if this space user can override current tiles (walkable or not) to get to the goal.
	 */
	public boolean overrideNextTile = false;
	public byte movementRetries = 0;
	
	private SpaceUserStatus[] m_statuses;
	public boolean m_requiresUpdate;
	
	/**
	 * User flat only. True if this SpaceUser is a controller in the flat it currently is in. Flat controllers can move furniture around (no picking up!) and can kick other users from the flat.
	 */
	public boolean isFlatController;
	/**
	 * User flat only. True if this SpaceUser is the owner of the flat it currently is in. Flat owners can do everything flat controllers can, but including picking up furniture etc.
	 */
	public boolean isFlatOwner;
	
	/**
	 * True if this SpaceUser is invisible for other clients.
	 */
	public boolean isInvisible;
	/**
	 * True if this SpaceUser walks reversed.
	 */
	public boolean isReverseWalk;
	
	/**
	 * True if this SpaceUser is a bot.
	 */
	private boolean m_isBot;
	
	public SpaceUser(User info)
	{
		m_isBot = true;
		m_comm = null;
		m_userObject = info;
		
		this.goalX = -1;
		this.path = new Vector<PathfinderNode>();
		m_statuses = new SpaceUserStatus[SpaceUser.MAX_SIMULTANEOUS_STATUSES];
	}
	
	/**
	 * Constructs a new SpaceUser.
	 * 
	 * @param client
	 */
	public SpaceUser(CommunicationHandler client)
	{
		m_isBot = false;
		m_comm = client;
		m_userObject = client.getUserObject();
		
		this.goalX = -1;
		this.path = new Vector<PathfinderNode>();
		m_statuses = new SpaceUserStatus[SpaceUser.MAX_SIMULTANEOUS_STATUSES];
	}
	
	/**
	 * Determines if this SpaceUser requires it's status broadcasted to space instance because it's updated.
	 * 
	 * @return True, False
	 */
	public boolean requiresUpdate()
	{
		if (m_requiresUpdate)
		{
			// Requires update no matter what the statuses say
			return true;
		}
		else
		{
			for (int i = 0; i < SpaceUser.MAX_SIMULTANEOUS_STATUSES; i++)
			{
				if (m_statuses[i] != null && m_statuses[i].isUpdated())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void ensureUpdate(boolean state)
	{
		m_requiresUpdate = state;
	}
	
	/**
	 * Adds a new SpaceUserStatus with given data. Old status with this name is removed. Omit data by supplying null, not an empty string.
	 * 
	 * @param name The name of the status.
	 * @param data The data of the status.
	 * @param lifeTimeSeconds The total amount of seconds this status lasts.
	 * @param action The action of the status, will be flipped with name etc.
	 * @param actionSwitchSeconds The total amount of seconds this action flips with the name.
	 * @param actionLengthSeconds The total amount of seconds that the action lasts before it flips back.
	 */
	public boolean addStatus(String name, String data, int lifeTimeSeconds, String action, int actionSwitchSeconds, int actionLengthSeconds)
	{
		// Remove old status
		removeStatus(name);
		
		// Allocate status
		for (int i = 0; i < SpaceUser.MAX_SIMULTANEOUS_STATUSES; i++)
		{
			if (m_statuses[i] == null)
			{
				m_statuses[i] = new SpaceUserStatus(name, data, lifeTimeSeconds, action, actionSwitchSeconds, actionLengthSeconds);
				m_requiresUpdate = true;
				return true;
			}
		}
		
		// Could not allocate
		return false;
	}
	
	/**
	 * Attempts to remove a status with a given name.
	 * 
	 * @param name The name of the status to remove. Case sensitive.
	 * @return True if status was removed, false if it was not found.
	 */
	public boolean removeStatus(String name)
	{
		for (int i = 0; i < SpaceUser.MAX_SIMULTANEOUS_STATUSES; i++)
		{
			if (m_statuses[i] != null && m_statuses[i].name.equals(name))
			{
				m_statuses[i] = null;
				m_requiresUpdate = true;
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if this user has a status with a given name.
	 * 
	 * @param name The name of the status to check. Case sensitive.
	 * @return True if this user has this status, False otherwise.
	 */
	public boolean hasStatus(String name)
	{
		for (int i = 0; i < SpaceUser.MAX_SIMULTANEOUS_STATUSES; i++)
		{
			if (m_statuses[i] != null && m_statuses[i].name.equals(name))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void removeInteractiveStatuses()
	{
		this.removeStatus("sit");
		this.removeStatus("lay");
	}
	
	/**
	 * Clears and resets this SpaceUsers path. (for walking in the space)
	 */
	public void clearPath()
	{
		this.goalX = -1;
		this.goalY = 0;
		this.path.clear();
	}
	
	public void wave()
	{
		if(!this.hasStatus("lay"))
		{
			this.removeStatus("dance");
			this.addStatus("wave", null, 2, null, 0, 0);
		}
	}
	
	public void angleHeadTo(short toX, short toY)
	{
		// Valid distance and context?
		if(this.isMoving() || this.hasStatus("lay"))
		{
			return;
		}
		
		// Calculate diff with current body rotation
		int diff = this.bodyRotation - RotationCalculator.calculateHumanDirection(this.X, this.Y, toX, toY);
		
		// Rotate the head if valid difference
		if((this.bodyRotation % 2) == 0)
		{
			if(diff > 0)
			{
				this.headRotation = (byte)(this.bodyRotation - 1);
			}
			else if(diff < 0)
			{
				this.headRotation = (byte)(this.bodyRotation + 1);
			}
			else
			{
				this.headRotation = this.bodyRotation;
			}
		}
		
		// Ensure a status update
		this.ensureUpdate(true);
	}
	
	/**
	 * Refreshes this SpaceUser's (real client) privileges in the flat, by setting the appropriate 'flatctrl' status and sending the appropriate network messages.
	 */
	public void refreshFlatPrivileges()
	{
		// Remove old status
		this.removeStatus("flatctrl");
		String flatControlValue = null;
		
		// Handle messaging
		if (this.isFlatController)
		{
			m_comm.sendMessage(new ServerMessage("YOUARECONTROLLER"));
			//flatControlValue = "onlyfurniture";
		}
		if (this.isFlatOwner)
		{
			m_comm.sendMessage(new ServerMessage("YOUAREOWNER"));
			flatControlValue = "useradmin";
		}
		
		// Add 'flatctrl' status
		if (this.isFlatController || this.isFlatOwner)
		{
			this.addStatus("flatctrl", flatControlValue, 0, null, 0, 0);
		}
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendKVArgument("a", Integer.toString(this.getUserObject().ID));
		msg.appendKVArgument("n", this.getUserObject().name);
		msg.appendKVArgument("f", this.getUserObject().figure);
		msg.appendKVArgument("s", Character.toString(this.getUserObject().sex));
		msg.appendKVArgument("l", this.X + " " + this.Y + " " + (int)this.Z);
		msg.appendKVArgument("c", this.getUserObject().motto);
		if (m_comm != null && m_comm.getSpaceInstance().getModel().hasSwimmingPool)
		{
			msg.appendKVArgument("p", this.getUserObject().poolFigure);
		}
		//if (this.isBot)
		//{
		//	msg.appendNewArgument("[bot]");
		//}
	}
	
	/**
	 * Updates this users temporarily statuses and appends the current status to a given ServerMessage.
	 * 
	 * @param msg The ServerMessage to write (append) the data to.
	 */
	public String getStatusString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append((char)13);
		
		sb.append(this.getUserObject().name);
		sb.append(' ');
		
		sb.append(this.X);
		sb.append(',');
		
		sb.append(this.Y);
		sb.append(',');
		
		sb.append((int)this.Z);
		sb.append(',');
		
		sb.append(this.headRotation);
		sb.append(',');
		
		sb.append(this.bodyRotation);
		sb.append('/');
		
		for (int i = 0; i < SpaceUser.MAX_SIMULTANEOUS_STATUSES; i++)
		{
			SpaceUserStatus status = m_statuses[i];
			if (status != null)
			{
				if (status.checkStatus())
				{
					sb.append(status.name);
					if (status.data != null)
					{
						sb.append(' ');
						sb.append(status.data);
					}
					sb.append('/');
				}
				else
				{
					m_statuses[i] = null;
				}
			}
		}
		
		return sb.toString();
	}
	
	public User getUserObject()
	{
		return m_userObject;
	}
	
	public CommunicationHandler getCommunicator()
	{
		return m_comm;
	}
	
	public boolean isBot()
	{
		return m_isBot;
	}
	
	public boolean isMoving()
	{
		return (this.goalX != -1);
	}
}
