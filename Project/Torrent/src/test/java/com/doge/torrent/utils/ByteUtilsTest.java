package com.doge.torrent.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ByteUtilsTest {

	@Test
	void testToByteWhenByteSizedIntIsPassed() {
		byte expected = 13;
		byte actual = ByteUtils.toByte(expected);
		assertEquals(expected, actual);
	}

	@Test
	void testToByteWhenIntLargerThanByteIsPassed() {
		byte expected = 13;
		byte actual = ByteUtils.toByte(256 + expected);
		assertEquals(expected, actual);
	}

}