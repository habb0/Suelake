package com.suelake.habbo.items;

import com.blunk.storage.DataQuery;

public abstract class ItemLoader implements DataQuery
{
	public int userID = 0;
	public int spaceID = 0;
	public String model = null;
}
