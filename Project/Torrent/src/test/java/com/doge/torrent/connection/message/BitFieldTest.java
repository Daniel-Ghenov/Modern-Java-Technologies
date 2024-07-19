package com.doge.torrent.connection.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BitFieldTest
{

	@ParameterizedTest(name = "hasPiece({0}) should return true")
	@ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
	void testHasPieceWhenNoPiecesShouldReturnFalse(int piece)
	{
		BitField bitField = new BitField(new byte[]{0});
		assertFalse(bitField.hasPiece(piece), "hasPiece(" + piece + ") should return false");
	}

	@Test
	void testHasPieceWhenPieceIsSetShouldReturnTrue()
	{
		BitField bitField = new BitField(new byte[]{(byte) 0b10000000});
		assertTrue(bitField.hasPiece(0), "hasPiece(0) should return true");
	}

	@Test
	void testHasPieceWhenPiece7IsSetShouldReturnTrue()
	{
		BitField bitField = new BitField(new byte[]{(byte) 0b00000001});
		assertTrue(bitField.hasPiece(7), "hasPiece(7) should return true");
	}

	@Test
	void testSetPieceWhenPiece0ShouldSetBit0()
	{
		BitField bitField = new BitField(new byte[]{0});
		bitField.setPiece(0);
		assertEquals(1, bitField.bitField()[0], "bitField[0] should be 1");
	}

	@Test
	void testSetPieceWhenPiece7ShouldSetBit7()
	{
		BitField bitField = new BitField(new byte[]{0});
		bitField.setPiece(7);
		assertEquals((byte) 0b10000000, bitField.bitField()[0], "bitField[0] should be 0b10000000");
	}

}