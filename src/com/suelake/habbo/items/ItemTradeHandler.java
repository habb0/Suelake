package com.suelake.habbo.items;

import java.util.Vector;

import com.suelake.habbo.HabboHotel;
import com.suelake.habbo.communication.CommunicationHandler;
import com.suelake.habbo.communication.SerializableObject;
import com.suelake.habbo.communication.ServerMessage;

/**
 * Provides 'trading Items' with other Users.\r Operates together with ItemInventoryHandler.
 * 
 * @author Nillus
 */
public class ItemTradeHandler implements SerializableObject
{
	private CommunicationHandler m_comm;
	private int m_partnerClientID;
	private Vector<Item> m_offer;
	private boolean m_accept;
	
	public ItemTradeHandler(CommunicationHandler comm)
	{
		m_comm = comm;
		m_offer = new Vector<Item>();
	}
	
	public void open(int partnerClientID)
	{
		m_partnerClientID = partnerClientID;
	}
	
	public void close()
	{
		// Clear variables
		m_accept = false;
		synchronized (m_offer)
		{
			m_offer.clear();
		}
		m_partnerClientID = 0;
		
		// Close window for client
		this.getClient().sendMessage(new ServerMessage("TRADE_CLOSE"));
		
		// Remove 'trading' status in space
		this.getClient().getSpaceInstance().getUserByClientID(this.getClient().clientID).removeStatus("trd");
	}
	
	public void accept()
	{
		m_accept = true;
	}
	
	public void unaccept()
	{
		m_accept = false;
	}
	
	public void offerItem(Item item)
	{
		m_accept = false;
		synchronized (m_offer)
		{
			m_offer.add(item);
		}
	}
	
	public void castOfferTo(ItemTradeHandler partner)
	{
		synchronized (m_offer)
		{
			for (Item item : m_offer)
			{
				// Remove item from 'my' inventory
				this.getClient().getItemInventory().removeItem(item.ID);
				
				// Add item to partner's inventory
				item.ownerID = partner.getClient().getUserObject().ID;
				partner.getClient().getItemInventory().addItem(item);
				
				// Update Item
				HabboHotel.getItemAdmin().updateItem(item);
			}
		}
	}
	
	public void refreshClients()
	{
		ItemTradeHandler partner = this.getPartner();
		if (partner != null)
		{
			this.refresh(partner);
			partner.refresh(this);
		}
	}
	
	public void refresh(ItemTradeHandler partner)
	{
		if (partner != null)
		{
			ServerMessage msg = new ServerMessage("TRADE_ITEMS");
			msg.appendObject(this);
			msg.appendObject(partner);
			
			this.getClient().sendMessage(msg);
		}
	}
	
	public CommunicationHandler getPartnerClient()
	{
		return HabboHotel.getGameClients().getClient(m_partnerClientID);
	}
	
	/**
	 * Retrieves the ItemTradeHandler of the trading partner client.
	 * 
	 * @return The ItemTradeHandler if retrieved, NULL otherwise.
	 */
	public ItemTradeHandler getPartner()
	{
		CommunicationHandler client = this.getPartnerClient();
		if (client != null)
		{
			return client.getItemTrader();
		}
		else
		{
			return null;
		}
	}
	
	public boolean accepting()
	{
		return m_accept;
	}
	
	/**
	 * True if this ItemTradeHandler is currently busy, False otherwise.
	 */
	public boolean busy()
	{
		return (m_partnerClientID != 0);
	}
	
	/**
	 * Returns the Vector collection holding the Item objects that this User currently offers in trade.
	 */
	public Vector<Item> getOffer()
	{
		return m_offer;
	}
	
	/**
	 * Returns the CommunicationHandler linked to this ItemTradeHandler.
	 */
	public CommunicationHandler getClient()
	{
		return m_comm;
	}
	
	@Override
	public void serialize(ServerMessage msg)
	{
		// 'User' and 'accept state'
		msg.appendTabArgument(m_comm.getUserObject().name);
		msg.appendTabArgument(Boolean.toString(m_accept));
		
		msg.append("\t");
		// Items offered by this User
		int index = 0;
		synchronized (m_offer)
		{
			for (Item item : m_offer)
			{
				item.serialize(msg, index++);
			}
		}
		
		// End of this 'box'
		msg.append("\r");
	}
}
