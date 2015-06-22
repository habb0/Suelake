package com.suelake.habbo.games;

import java.util.Random;

import com.blunk.util.TimeHelper;
import com.suelake.habbo.communication.ServerMessage;
import com.suelake.habbo.spaces.instances.SpaceInstance;
import com.suelake.habbo.spaces.instances.SpaceUser;

/**
 * WobbleSquabble ('paalu') is a ingame game where two Users battle against each other.\r Both players are wearing swimming clothes and are standing on inflated balloons and try to wack each other off the balloons.\r Users that wait for their turn are in a 'queue'.
 * 
 * @author Nillus
 */
public class WobbleSquabbleHandler implements Runnable
{
	public final static short WS_GAME_TICKET_COST = 1;
	public final static byte WS_BALANCE_POINTS = 35;
	public final static byte WS_HIT_POINTS = 13;
	public final static byte WS_HIT_BALANCE_POINTS = 10;
	public final static long WS_GAME_TIMEOUT_MS = 60000; // Timeout in one minute
	
	private Random m_random;
	private SpaceInstance m_spaceInstance;
	private WobbleSquabblePlayer[] m_players;
	private Thread m_thread;
	private boolean m_gameActive;
	
	public WobbleSquabbleHandler(SpaceInstance instance)
	{
		m_random = new Random();
		m_spaceInstance = instance;
		m_players = new WobbleSquabblePlayer[2];
	}
	
	public void start()
	{
		// Create thread
		m_thread = new Thread(this, "WobbleSquabble in space " + this.getSpaceInstance().getInfo().ID);
		m_thread.setPriority(Thread.MIN_PRIORITY);
		
		// Start thread
		m_gameActive = true;
		m_thread.start();
	}
	
	public void stop()
	{
		// Stop thread
		m_gameActive = false;
		if (m_thread != null)
		{
			m_thread.interrupt();
		}
	}
	
	/**
	 * Runnable. Ticks the handler every 100ms to refresh players and the waiting queue.
	 */
	public void run()
	{
		// Countdown...
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException ex1)
		{
			return;
		}
		
		// Position players
		m_players[0].position = -2;
		m_players[1].position = 3;
		
		// Prepare stance etc
		m_players[0].prepare();
		m_players[1].prepare();
		
		// Notify clients game started
		ServerMessage msg = new ServerMessage("PT_ST");
		msg.appendKV2Argument("0", m_players[0].getUser().getUserObject().name);
		msg.appendKV2Argument("1", m_players[1].getUser().getUserObject().name);
		this.getSpaceInstance().broadcast(msg);
		
		// Keep running till timeout
		long endTime = TimeHelper.getTime() + WobbleSquabbleHandler.WS_GAME_TIMEOUT_MS;
		while (m_gameActive && TimeHelper.getTime() < endTime)
		{
			// Update players?
			if (m_players[0].requiresUpdate || m_players[1].requiresUpdate)
			{
				// Process move
				this.updatePlayer(m_players[0]);
				this.updatePlayer(m_players[1]);
				
				// Send updates
				msg.set("PT_SI");
				msg.appendObject(m_players[0]);
				msg.appendObject(m_players[1]);
				this.getSpaceInstance().broadcast(msg);
				
				// Reset players (actions processed)
				m_players[0].reset();
				m_players[1].reset();
				
				// Determine loser
				byte loserID = -1;
				byte winnerID = -1;
				if (!m_players[0].balanceOK())
				{
					winnerID = 1;
					loserID = 0;
				}
				if (!m_players[1].balanceOK())
				{
					// Other player still standing?
					if (loserID == -1)
					{
						winnerID = 0;
						loserID = 1;
					}
					else
					{
						// Draw!
						loserID = 2;
					}
				}
				
				// Handle loser
				if (loserID != -1)
				{
					// Draw?
					if (loserID == 2)
					{
						break;
					}
					else
					{
						// Notify clients of winner
						msg.set("PT_WIN");
						msg.appendArgument(Byte.toString(winnerID));
						this.getSpaceInstance().broadcast(msg);
						
						// Remove loser
						this.removePlayer(loserID);
						
						// Wait while clients display falling animation
						try
						{
							Thread.sleep(1500);
						}
						catch (InterruptedException ex)
						{
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						
						// End the game
						this.endGame();
						return;
					}
				}
			}
			
			// Sleep...
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException ex)
			{
				// Thread aborted!
				return;
			}
		}
		
		// Draw! (timeout)
		msg.set("PT_BOTHLOSE");
		this.getSpaceInstance().broadcast(msg);
		
		// Kick both players
		this.removePlayer((byte)0);
		this.removePlayer((byte)1);
		
		// End the game thread etc
		this.endGame();
	}
	
	public void setPlayer(byte playerID, SpaceUser usr)
	{
		// Create player for this user
		WobbleSquabblePlayer player = new WobbleSquabblePlayer(playerID, usr, this);
		
		// Set this player
		m_players[playerID] = player;
		
		// Two players?
		if (m_players[0] != null && m_players[1] != null)
		{
			// Announce player setup to clients
			ServerMessage msg = new ServerMessage("PT_PR");
			msg.appendKV2Argument("0", m_players[0].getUser().getUserObject().name);
			msg.appendKV2Argument("1", m_players[1].getUser().getUserObject().name);
			this.getSpaceInstance().broadcast(msg);
			
			// Start game!
			this.start();
		}
	}
	
	private void updatePlayer(WobbleSquabblePlayer player)
	{
		// Get opponent
		WobbleSquabblePlayer opponent = m_players[((player.ID == 0) ? 1 : 0)];
		
		// Determine move player made
		switch (player.move)
		{
			// Balance left/right
			case 'A':
			case 'D':
			{
				// Calculate balance delta
				byte delta = (byte)(WobbleSquabbleHandler.WS_BALANCE_POINTS + this.getRandom().nextInt(10));
				
				// Apply balance delta to player
				player.balance += (delta * player.moveDirection);
				break;
			}
				
				// Hit left/right
			case 'E':
			case 'W':
			{
				// Standing next to opponent?
				if (Math.abs(player.position - opponent.position) == 1)
				{
					// Opponent has been hit!
					opponent.beenHit = true;
					
					// Calculate balance delta to opponent
					byte delta = (byte)(WobbleSquabbleHandler.WS_HIT_POINTS + this.getRandom().nextInt(10));
					opponent.balance += (delta * player.moveDirection);
				}
				else
				{
					// Wacking in the void! This costs player balance!
					byte delta = (byte)(WobbleSquabbleHandler.WS_HIT_BALANCE_POINTS + this.getRandom().nextInt(10));
					player.balance += (delta * player.moveDirection);
				}
				break;
			}
				
			// Move (walk) forward/backward
			case 'S':
			case 'X':
			{
				// Calculate new position
				byte newPosition = (byte)(player.position + player.moveDirection);
				
				// Is this position in range?
				if (newPosition > -3 && newPosition < 3)
				{
					// Opponent not on this position already?
					if (newPosition != opponent.position)
					{
						player.position = newPosition;
					}
				}
				break;
			}
				
			// Restore balance
			case '0':
			{
				if (!player.usedReBalance)
				{
					player.balance = 0;
					player.usedReBalance = true;
				}
				break;
			}
		}
	}
	
	private void endGame()
	{
		// Broadcast clients that game has ended
		ServerMessage msg = new ServerMessage("PT_EN");
		this.getSpaceInstance().broadcast(msg);
		
		// Stop game worker
		this.stop();
	}
	
	/**
	 * Removes the WobbleSquabblePlayer with a given player ID from the game, deducting tickets and pushing off the inflatables.
	 * 
	 * @param playerID The player ID of the player to remove.
	 */
	private void removePlayer(byte playerID)
	{
		// Get player
		WobbleSquabblePlayer player = m_players[playerID];
		
		// Deduct tickets
		player.getUser().getCommunicator().getUserObject().gameTickets -= WobbleSquabbleHandler.WS_GAME_TICKET_COST;
		player.getUser().getCommunicator().sendGameTickets();
		
		// Drop player (user) in pool
		player.getUser().moveLock = false;
		player.getUser().addStatus("swim", null, 0, null, 0, 0);
		this.getSpaceInstance().getInteractor().warpUser(player.getUser(), (short)(player.getUser().X + ((player.balance < 0) ? -1 : 1)), player.getUser().Y, true);
		this.getSpaceInstance().getInteractor().refreshUserStatus(player.getUser());
		
		// Remove player
		m_players[playerID] = null;
	}
	
	/**
	 * Returns the WobbleSquabblePlayer representing the player with a given player ID.
	 * 
	 * @param playerID The 'ID' of the player, 0 = left, 1 = right.
	 * @return The WobbleSquabblePlayer representing the player, or NULL if there is no player with that player ID.
	 */
	public WobbleSquabblePlayer getPlayer(int playerID)
	{
		return m_players[playerID];
	}
	
	public WobbleSquabblePlayer getPlayerByClientID(int clientID)
	{
		for (int i = 0; i <= 1; i++)
		{
			if (m_players[i] != null)
			{
				if (m_players[i].getUser().getCommunicator().clientID == clientID)
				{
					return m_players[i];
				}
			}
		}
		
		return null;
	}
	
	public boolean gameRunning()
	{
		return m_gameActive;
	}
	
	/**
	 * Returns the SpaceInstance that 'hosts' this game.
	 */
	public SpaceInstance getSpaceInstance()
	{
		return m_spaceInstance;
	}
	
	public Random getRandom()
	{
		return m_random;
	}
}
