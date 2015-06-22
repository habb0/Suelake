package com.suelake.habbo.messenger;

import com.blunk.storage.DataObject;

public class MessengerBuddyLink implements DataObject
{
	public int ID1;
	public int ID2;
	public boolean isAccepted;
	
	@Override
	public long getCacheKey()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
