package com.suelake.habbo.economy;

import java.util.Date;

import com.blunk.storage.DataObject;
import com.blunk.util.TimeHelper;

public class Stock implements DataObject
{
	public int ID;
	public String companyID;
	public int userID;
	public short credits;
	public Date moment;
	
	private long getAgeMillis()
	{
		return TimeHelper.getTime() - this.moment.getTime();
	}
	
	public boolean isRedeemable()
	{
		return (getAgeMillis() > (VirtualEconomy.STOCK_REDEEM_HOURS * 60 * 60 * 1000));
	}
	
	public String getTimeLeft()
	{
		long timeLeft = (VirtualEconomy.STOCK_REDEEM_HOURS * 60 * 60 * 1000) - this.getAgeMillis();
		if (timeLeft > 0)
		{
			long minutes = (timeLeft / (1000 * 60)) % 60;
			long hours = (timeLeft / (1000 * 60 * 60)) % 24;
			
			return hours + "h and " + minutes + "m";
		}
		else
		{
			return "0h and 0m";
		}
		//Date d = new Date(timeLeft);
		//return d.getHours() + "h and " + d.getMinutes() + "m";
		
		/*Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(TimeHelper.getTime());//(VirtualEconomy.STOCK_REDEEM_HOURS * 60 * 60 * 1000) - this.getAgeMillis());
		return cal.get(Calendar.HOUR) + "h and " + cal.get(Calendar.MINUTE) + "m";*/
	}
	
	@Override
	public long getCacheKey()
	{
		return this.ID;
	}
}
