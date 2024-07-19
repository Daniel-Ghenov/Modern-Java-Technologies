package com.doge.torrent.files.bencode;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BencodeInputStreamTest
{

	@Test
	void testReadStringWhenStringIsNotEmpty() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("4:spam".getBytes())))
		{
			assertEquals("spam", bencodeInputStream.readString());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadStringWhenTokenInvalid() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("h4spam".getBytes())))
		{
			assertThrows(IOException.class, bencodeInputStream::readString);
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadNumberWhenTokenInvalidShouldThrow() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("h".getBytes())))
		{
			assertThrows(IOException.class, bencodeInputStream::readNumber);
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadNumberWhenNumberIsNotEmpty() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("i123e".getBytes())))
		{
			assertEquals(123L, bencodeInputStream.readNumber());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadListWhenInvalidTokenShouldThrow() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("h4:spam".getBytes())))
		{
			assertThrows(IOException.class, bencodeInputStream::readList);
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadListWhenTokenIsNotEmpty() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("l4:spam4:eggse".getBytes())))
		{
			assertIterableEquals(List.of("spam", "eggs"), bencodeInputStream.readList());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testReadDictionaryWhenInvalidTokenShouldThrow() {
		try (BencodeInputStream bencodeInputStream = new BencodeInputStream(new ByteArrayInputStream("h4:spam".getBytes())))
		{
			assertThrows(IOException.class, bencodeInputStream::readDictionary);
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

}