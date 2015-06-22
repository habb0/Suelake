package com.suelake.habbo.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.blunk.Log;
import com.suelake.habbo.communication.ClientManager;

/**
 * InfoConnectionListener binds to a TCP port to listen for incoming InfoConnections. New InfoConnections are accepted and a CommunicationHandler is wrapped around it, before being entered in a ClientManager.
 * 
 * @author Nillus
 */
public class InfoConnectionListener implements Runnable
{
	private int m_port;
	private boolean m_alive;
	private Thread m_thread;
	private ClientManager m_clientManager;
	
	public InfoConnectionListener(int port, ClientManager clientManager)
	{
		m_port = port;
		m_clientManager = clientManager;
	}
	
	public void start()
	{
		if (!m_alive)
		{
			// Create thread
			m_thread = new Thread(this, "InfoConnectionListener");
			m_thread.setPriority(Thread.MIN_PRIORITY);
			
			// Start thread
			m_alive = true;
			m_thread.start();
		}
	}
	
	public void stop()
	{
		if(m_alive)
		{
			// Interrupt thread
			m_alive = false;
			m_thread.interrupt();
		}
	}
	
	public void run()
	{
		ServerSocket listener;
		try
		{
			listener = new ServerSocket(m_port);
		}
		catch (IOException ex)
		{
			Log.error("InfoConnectionListener: could not bind listener socket to local TCP port " + m_port + "! Port/address probably in use.");
			this.stop();
			return;
		}
		
		// Keep accepting connections
		while(m_alive)
		{
			try
			{
				// Create new InfoConnection based on socket
				Socket socket = listener.accept();
				InfoConnection connection = new InfoConnection(socket);
				
				// Pass the new connection to the ClientManager
				m_clientManager.handleAcceptedConnection(connection);
			}
			catch (IOException ex)
			{
				Log.error("InfoConnectionListener: error accepting new InfoConnection!", ex);
			}
		}
		
		// Close listener
		try
		{
			listener.close();
		}
		catch (IOException ex)
		{
			Log.error("InfoConnectionListener: error closing listener socket", ex);
		}
	}
	
	public int getPort()
	{
		return m_port;
	}
	
	public ClientManager getClientManager()
	{
		return m_clientManager;
	}
}
