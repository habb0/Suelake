package com.suelake.habbo.economy;

import java.util.Vector;

import com.blunk.Environment;
import com.blunk.util.TimeHelper;
import com.suelake.habbo.HabboHotel;

public class VirtualEconomy
{
	@SuppressWarnings("unchecked")
	private Class m_companyClass;
	@SuppressWarnings("unchecked")
	private Class m_StockClass;
	
	public final static int STOCK_MIN = 5;
	public final static int STOCK_MAX = 50;
	public final static int STOCK_REDEEM_HOURS = 6;
	public final static float STOCK_REDEEM_MAXGAIN = 1.35f;
	
	public final static float COMPANY_SCALEMOD = 0.05f;
	public final static float COMPANY_SCALE_MIN = 1.0f;
	public final static float COMPANY_SCALE_MAX = 10.0f;

	public final static float COMPANY_RISKMOD = 0.05f;
	public final static float COMPANY_RISK_MIN = 0.1f;
	public final static float COMPANY_RISK_MAX = 1.0f;
	
	public VirtualEconomy()
	{
		Company sample = (Company)HabboHotel.getDataObjectFactory().newObject("Company");
		if (sample != null)
		{
			m_companyClass = sample.getClass();
		}
		
		Stock sample2 = (Stock)HabboHotel.getDataObjectFactory().newObject("Stock");
		if (sample2 != null)
		{
			m_StockClass = sample2.getClass();
		}
	}
	
	public Company getCompany(String companyID)
	{
		Company company = this.newCompany();
		company.ID = companyID;
		if(Environment.getDatabase().load(company))
		{
			return company;
		}
		else
		{
			return null;
		}
	}
	
	public Stock getStock(int stockID)
	{
		Stock stock = this.newStock();
		stock.ID = stockID;
		if(Environment.getDatabase().load(stock))
		{
			return stock;
		}
		else
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Stock> getStocksForUser(int userID)
	{
		StockFinder finder = (StockFinder)HabboHotel.getDataQueryFactory().newQuery("StockFinder");
		finder.userID = userID;
		
		return (Vector<Stock>)Environment.getDatabase().query(finder);
	}
	
	public Stock makeInvestment(int userID, Company company, short credits)
	{
		Stock stock = this.newStock();
		stock.companyID = company.ID;
		stock.userID = userID;
		stock.credits = credits;
		stock.moment = TimeHelper.getDateTime();
		
		if(Environment.getDatabase().insert(stock))
		{
			// This stock is good for the company!
			company.funds += credits;
			company.scale += ((company.scale * (stock.credits / 100)) / stock.credits);
			if(company.scale > COMPANY_SCALE_MAX) company.scale = COMPANY_SCALE_MAX;
			Environment.getDatabase().update(company);
			
			return stock;
		}
		else
		{
			return null;
		}
	}
	
	public short redeemStock(Stock stock)
	{
			// Get company
			Company company = this.getCompany(stock.companyID);
			
			// What happened?
			short result;
			if(Environment.getRandom().nextFloat() <= company.risk)
			{
				// Loss, company's risk increases
				result = (short)(stock.credits / company.scale);
				company.risk += ((COMPANY_RISKMOD) * stock.credits) / 10;
				
				// Ensure that the risk stays within the maximum risk etc
				if(result < 0) result = 0;
				if(company.risk > COMPANY_RISK_MAX) company.risk = COMPANY_RISK_MAX;
			}
			else
			{
				// Gain, company's risk decreases and scale increases
				result = (short)(stock.credits * company.scale);
				company.risk -= COMPANY_RISKMOD;
				
				// Ensure that the risk stays within the minimum risk etc
				if(result > (stock.credits * STOCK_REDEEM_MAXGAIN)) result = (short)(stock.credits * STOCK_REDEEM_MAXGAIN);
				if(company.risk < COMPANY_RISK_MIN) company.risk = COMPANY_RISK_MIN;
			}
			
			// Update company and delete the stock
			Environment.getDatabase().update(company);
			Environment.getDatabase().delete(stock);
			
			// Return the amount of credits the invester gets back from his stock
			return result;
	}
	
	public Company newCompany()
	{
		try
		{
			return (Company)m_companyClass.newInstance();
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
	
	public Stock newStock()
	{
		try
		{
			return (Stock)m_StockClass.newInstance();
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
}
