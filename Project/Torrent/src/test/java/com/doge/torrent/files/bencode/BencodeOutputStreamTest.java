package com.doge.torrent.files.bencode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BencodeOutputStreamTest
{

	BencodeOutputStream os;

	ByteArrayOutputStream output;

	@BeforeEach
	void setUp() {
		output = new ByteArrayOutputStream();
		os = new BencodeOutputStream(output);
	}


	@Test
	void testWriteString() {
		String input = "spam";
		try {
			os.writeString(input);
			assertEquals("4:spam", output.toString());
		}
		catch (Exception e) {
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testWriteLong() {
		Long input = 40296L;

		try {
			os.writeLong(input);
			assertEquals("i40296e", output.toString());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}

	}

	@Test
	void testWriteList() {

		List<String> input = List.of("spam", "eggs");

		try {
			os.writeList(input);
			assertEquals("l4:spam4:eggse", output.toString());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}

	}

	@Test
	void testWriteDictionary() {

		Map<String, Object> map = Map.of("cow", "moo", "spam", "eggs");

		try {
			os.writeDictionary(map);
			assertEquals("d3:cow3:moo4:spam4:eggse", output.toString());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}

	@Test
	void testWriteDictionaryWhenOutOfOrderShouldSort() {
		Map<String, Object> map = Map.of("spam", "eggs", "cow", "moo");

		try {
			os.writeDictionary(map);
			assertEquals("d3:cow3:moo4:spam4:eggse", output.toString());
		}
		catch (Exception e)
		{
			fail("Unexpected exception was thrown");
		}
	}
}