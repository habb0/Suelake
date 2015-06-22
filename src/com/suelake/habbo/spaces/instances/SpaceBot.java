package com.suelake.habbo.spaces.instances;

import com.suelake.habbo.users.User;

public class SpaceBot extends SpaceUser
{
	public int ownerID;
	public long lastAction;
	private byte m_mode;
	
	public SpaceBot(User info)
	{
		super(info);
	}
	
	public void setMode(byte mode)
	{
		m_mode = mode;
	}
	public byte getMode()
	{
		return m_mode;
	}
}
