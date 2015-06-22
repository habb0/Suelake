package com.suelake.habbo.games;

import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.spaces.instances.SpaceUser;

/**
 * WobbleSquabblePlayer is a wrapper around a SpaceUser that is currently playing WobbleSquabble.\r
 * WobbleSquabblePlayer holds score, balance, position etc.
 * 
 * @author Nillus
 * @see WobbleSquabbleHandler
 */
public class WobbleSquabblePlayer implements SerializableObject
{
	public byte ID;
	private SpaceUser m_usr;
	private WobbleSquabbleHandler m_handler;
	
	public byte position;
	public byte balance;
	public boolean beenHit;
	public char move;
	public byte moveDirection;
	public boolean requiresUpdate;
	public boolean usedReBalance;
	
	public WobbleSquabblePlayer(byte playerID, SpaceUser usr, WobbleSquabbleHandler handler)
	{
		this.ID = playerID;
		m_usr = usr;
		m_handler = handler;
	}
	
	public boolean balanceOK()
	{
		return (this.balance > -100 && this.balance < 100);
	}
	
	public void prepare()
	{
		this.move = '-';
		this.balance = 0;
		
		this.usedReBalance = false;
		this.requiresUpdate = true;
	}
	
	public void reset()
	{
		this.beenHit = false;
		this.move = '-';
		this.requiresUpdate = false;
	}
	
	public SpaceUser getUser()
	{
		return m_usr;
	}
	
	public WobbleSquabbleHandler getHandler()
	{
		return m_handler;
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		msg.appendNewArgument(Byte.toString(this.position));
		msg.appendTabArgument(Byte.toString(this.balance));
		msg.appendTabArgument(Character.toString(this.move));
		msg.appendTabArgument((this.beenHit ? "h" : ""));
	}
}
