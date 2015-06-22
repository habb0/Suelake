package com.suelake.habbo.economy.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.blunk.storage.sql.SQLDataQuery;
import com.suelake.habbo.economy.Stock;
import com.suelake.habbo.economy.StockFinder;

public class SQLStockFinder extends StockFinder implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
	}
	
	@Override
	public Vector<?> query(Connection conn) throws SQLException
	{
		PreparedStatement query = conn.prepareStatement("SELECT * FROM economy_stocks WHERE userid = ? ORDER BY moment ASC;");
		query.setInt(1, super.userID);
		
		// Parse results
		ResultSet result = query.executeQuery();
		Vector<Stock> stocks = new Vector<Stock>();
		while(result.next())
		{
			SQLStock stock = new SQLStock();
			if(SQLStock.parseFromResultSet(stock, result))
			{
				stock.ID = result.getInt("id");
				stocks.add(stock);
			}
		}
		
		query.close();
		return stocks;
	}
}
