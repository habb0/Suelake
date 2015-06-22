package com.suelake.habbo.economy;

import com.blunk.storage.DataObject;

public class Company implements DataObject
{
	public String ID;
	public String displayName;
	public float risk;
	public float scale;
	public int funds;
	
	@Override
	public long getCacheKey()
	{
		return 0;
	}
	
	public String toString()
	{
		return "[" + this.ID + "] " + this.displayName;
	}
}
