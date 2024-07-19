package com.doge.torrent.utils;

public class ByteUtils {

	private static final int BYTE_MASK = 0xFF;

	public static byte toByte(int i) {
		return (byte) (i & BYTE_MASK);
	}

	public static int toUnsignedByte(byte b) {
		return b & BYTE_MASK;
	}

}
