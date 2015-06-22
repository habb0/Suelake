package com.suelake.habbo.util;

/**
 * Provides methods for encoding and decoding content for the 'room directory' message.
 * Complete decoding written by Mike.
 * @author Mike / Nillus
 *
 */
public class RoomDirectoryEncoding
{
	/**
	 * Encodes a 32 bit integer in this weird encoding.
	 * @param i The integer to encode.
	 * @return A byte array of 4 elements.
	 */
	public static byte[] encodeInt(int i)
	{
		byte[] data = new byte[4];
		data[0] = (byte)((i & 127) | 128); // A
		data[1] = (byte)(((i / 128) & 127) | 128); // B
		data[2] = (byte)(((i / 16384) & 127) | 128); // C
		data[3] = (byte)(((i / 2097152) & 127) | 128); // D
		
		return data;
	}
	/**
	 * Decodes 4 bytes to their original 32 bit integer.
	 * @param A Meep
	 * @param B Nibble
	 * @param C Neep
	 * @param D Mibble
	 * @return The decoded integer.
 	 */
	public static int decodeInt(byte A, byte B, byte C, byte D)
	{
		// Here comes the fun part :-)
		int tL1 = (int)(A & 127);
		int tL2 = (int)(B & 127);
		int tL3 = (int)(C & 127);
		int tL4 = (int)(D & 127);
		
		return (tL4 * integralPow(128, 3) + tL3 * integralPow(128, 2) + tL2 * (128) + tL1);
		
		/* 'Brute force' 0 to 10000
		for(int i = 0; i < 10000; i++)
		{
			byte[] encoded = RoomDirectoryEncoding.encodeInt(i);
			if(encoded[0] == A && encoded[1] == B && encoded[2] == C && encoded[3] == D)
				return i;
		}
		
		return -1;*/
	}
	public static char[] encode(int roomID, int doorID, boolean isFlat)
	{
		byte[] roomVal = RoomDirectoryEncoding.encodeInt(roomID);
		byte[] doorVal = RoomDirectoryEncoding.encodeInt(doorID);
		char[] encoded = new char[9];
		
		encoded[0] = (char)(isFlat ? 128 : 129);
		
		encoded[1] = (char)roomVal[3]; // tL4
		encoded[2] = (char)roomVal[2]; // tL3
		encoded[3] = (char)roomVal[1]; // tL2
		encoded[4] = (char)roomVal[0]; // tL1
		
		encoded[5] = (char)doorVal[3]; // tL8
		encoded[6] = (char)doorVal[2]; // tL7
		encoded[7] = (char)doorVal[1]; // tL6
		encoded[8] = (char)doorVal[0]; // tL5
		
		return encoded;
	}
	
	public static int[] decode(char[] encoded)
	{
		int[] result = new int[3]; // typeID, roomID and doorID
		result[0] = encoded[0]; // typeID
		result[1] = decodeInt((byte)encoded[4], (byte)encoded[3], (byte)encoded[2], (byte)encoded[1]); // roomID
		result[2] = decodeInt((byte)encoded[8], (byte)encoded[7], (byte)encoded[6], (byte)encoded[5]); // doorID
		
		return result;
	}
	
	public static int[] decodeMike(char[] encoded)
	{
		int[] result = new int[3];
		result[0] = encoded[0];	// tTypeID
		
		// Here comes the fun part :-)
		int tL1 = (int)(encoded[4] & 127);	// We start at 4 cuz' the first encoded char is typeID
		int tL2 = (int)(encoded[3] & 127);
		int tL3 = (int)(encoded[2] & 127);
		int tL4 = (int)(encoded[1] & 127);
		
		result[1] = (tL4 * integralPow(128, 3) + tL3 * integralPow(128, 2) + tL2 * (128) + tL1);
		result[2] = 0;
		
		return result;
	}
	
	private static int integralPow(int i, int power)
	{
		for (int x = 0; x < power; x++)
		{
			i *= power;
		}
		return i;
	}
}
