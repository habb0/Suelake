package com.suelake.habbo.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import com.blunk.Log;
import com.suelake.habbo.communication.CommunicationHandler;

/**
 * InfoConnection is a network layer wrapper for a CommunicationHandler. Mac/Windows character encoding detection by Jeax.
 * 
 * @author Nillus
 */
public class InfoConnection implements Runnable
{
	// Encoding (Latin1 for Windows, UTF-8 for Mac)
	public final static String DATA_ENCODING_WIN = "iso-8859-1";
	public final static String DATA_ENCODING_MAC = "UTF-8";
	
	// Connection
	private Socket m_socket;
	private Thread m_thread;
	private boolean m_alive;
	
	// Data reading & writing
	private BufferedReader m_in;
	private BufferedWriter m_out;
	
	// Client
	private CommunicationHandler m_client;
	private boolean m_macintosh;
	private boolean m_firstData = true;
	
	public InfoConnection(Socket socket)
	{
		m_socket = socket;
	}
	
	/**
	 * Creates the I/O streams on the socket and starts the thread.
	 * 
	 * @return True if started successfully, false otherwise.
	 */
	public boolean start(CommunicationHandler client)
	{
		try
		{
			// Greet client
			//byte[] greet = { '#', 'H', 'E', 'L', 'L', 'O', '#', '#' };
			//m_socket.getOutputStream().write(greet);
			byte[] greet = new byte[8];
			greet[0] = (byte)'#';
			greet[1] = (byte)'H';
			greet[2] = (byte)'E';
			greet[3] = (byte)'L';
			greet[4] = (byte)'L';
			greet[5] = (byte)'O';
			greet[6] = (byte)'#';
			greet[7] = (byte)'#';
			m_socket.getOutputStream().write(greet);
			// Determine whether it's Mac or Windows
			m_macintosh = (m_socket.getInputStream().read() == 194);
			
			// Determine the appropriate character encoding
			String dataEncoding = (m_macintosh) ? DATA_ENCODING_MAC : DATA_ENCODING_WIN;
			
			// And then setup I/O for this data encoding
			m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream(), dataEncoding));
			m_out = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream(), dataEncoding));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		
		// Create and start thread
		m_alive = true;
		m_thread = new Thread(this, "Client/TcpConnection #" + client.clientID);
		m_thread.setPriority(Thread.MIN_PRIORITY);
		
		// OK
		m_client = client;
		return true;
	}
	
	public void startThread()
	{
		m_thread.start();
	}
	
	public void stop()
	{
		// Alive?
		if (m_alive)
		{
			// End thread
			m_alive = false;
			m_thread.interrupt();
			
			// Close socket
			try
			{
				m_socket.close();
			}
			catch (Exception ex)
			{
				
			}
			
			// Close streams
			try
			{
				m_in.close();
				m_out.close();
			}
			catch (Exception ex)
			{
				
			}
		}
	}
	
	public void sendData(char[] data, int offset, int length)
	{
		try
		{
			m_out.write(data, offset, length);
			m_out.flush();
		}
		catch (IOException ex)
		{
			m_client.stop("send error");
		}
	}
	
	public void sendData(String str)
	{
		try
		{
			m_out.write(str);
			m_out.flush();
		}
		catch (IOException ex)
		{
			m_client.stop("send error");
		}
	}
	
	public void run()
	{
		while (m_alive)
		{
			try
			{
				// If is first ever packet fix it up because of mac-hax (ONCE PER CONNECTION ON FIRST DATA ARRIVAL)
				char[] header = new char[5];
				for (int i = 0; i < header.length; i++)
				{
					header[i] = (char)m_in.read();
					if(m_firstData)
					{
						header[0] = 128;
						if(!m_macintosh)
						{
							header[1] = 128;
							i++;
						}
						m_firstData = false;
					}
				}
				// Determine message type and message length
				int msgType = header[0] + header[1];
				int msgLength = ((header[4] - 128) + ((header[3] - 128) * 128) + ((header[2] - 128) * 16384));
				
				// Disconnected? Or hax?
				if (msgLength <= 0 || msgLength > 1024)
				{
					m_alive = false;
				}
				else
				{
					// Read in actual data and verify if all data has been read
					char[] msgData = new char[msgLength];
					if (m_in.read(msgData) != msgLength)
					{
						// No message broking!
						m_alive = false;
					}
					else
					{
						m_client.handleIncomingNetworkData(msgType, msgData);
					}
				}
			}
			catch (EOFException ex)
			{
				// No more data to read, end of stream = end of connection
				m_alive = false;
			}
			catch (SocketException ex)
			{
				// Remote end closed connection, this usually happens when; client A on machine X is logged in to account 1, client B on machine X logs in to account 1
				m_alive = false;
			}
			catch (Exception ex)
			{
				Log.error("Client/InfoConnection #" + m_client.clientID + ": abnormal error!", ex);
				m_client.systemMsg("SERVER ERROR\r" + ex);
			}
		}
		
		// Connection is no longer alive? Tell 'em souljaboy!
		m_client.stop("disconnected by user");
	}
	
	public boolean isMacintosh()
	{
		return m_macintosh;
	}
	
	public String getIpAddress()
	{
		return m_socket.getInetAddress().getHostAddress();
	}
	
	public String getHostName()
	{
		return m_socket.getInetAddress().getHostName();
	}
}