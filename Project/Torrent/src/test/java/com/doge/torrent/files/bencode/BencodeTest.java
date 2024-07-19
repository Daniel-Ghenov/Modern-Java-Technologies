package com.doge.torrent.files.bencode;

import com.doge.torrent.files.bencode.exception.BencodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BencodeTest {

	private Bencode bencode = new Bencode();

	@BeforeEach
	void setUp() {
		bencode = new Bencode();
	}


	@Test
	void testGetTypeWhenNextTypeNumber() {
		String input = "i123e";

		BencodeType<?> type = bencode.getType(input.getBytes());

		assertEquals(BencodeType.bencodeNumber, type);
	}

	@Test
	void testGetTypeWhenNextTypeString() {
		String input = "4:spam";

		BencodeType<?> type = bencode.getType(input.getBytes());

		assertEquals(BencodeType.bencodeString, type);
	}

	@Test
	void testGetTypeWhenNextTypeList() {
		String input = "l4:spam4:eggse";

		BencodeType<?> type = bencode.getType(input.getBytes());

		assertEquals(BencodeType.bencodeList, type);
	}

	@Test
	void testGetTypeWhenNextTypeDictionary() {
		String input = "d3:cow3:moo4:spam4:eggse";

		BencodeType<?> type = bencode.getType(input.getBytes());

		assertEquals(BencodeType.bencodeDictionary, type);
	}

	@Test
	void testGetTypeWhenNextTypeUnknown() {
		String input = "x";

		BencodeType<?> type = bencode.getType(input.getBytes());

		assertEquals(BencodeType.bencodeUnknown, type);
	}

	@Test
	void testDecodeWhenTypeNullShouldThrow() {
		String input = "i123e";

		assertThrows(BencodeException.class, () -> bencode.decode(input, null));
	}

	@Test
	void testDecodeWhenTypeUnknownShouldThrow() {
		String input = "i123e";

		assertThrows(BencodeException.class, () -> bencode.decode(input, BencodeType.bencodeUnknown));
	}

	@Test
	void testDecodeWhenBencodeNullShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.decode((String) null, BencodeType.bencodeNumber));
	}

	@Test
	void testDecodeWhenTypeNullAndByteArrShouldThrow() {
		byte[] input = "i123e".getBytes();

		assertThrows(BencodeException.class, () -> bencode.decode(input, null));
	}

	@Test
	void testDecodeWhenTypeUnknownAndByteArrShouldThrow() {
		byte[] input = "i123e".getBytes();

		assertThrows(BencodeException.class, () -> bencode.decode(input, BencodeType.bencodeUnknown));
	}

	@Test
	void testDecodeWhenBencodeNullAndByteArrShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.decode((byte[]) null, BencodeType.bencodeNumber));
	}

	@Test
	void testDecodeWhenTypeNumberAndPositive() {
		String input = "i123e";

		Long num = bencode.decode(input, BencodeType.bencodeNumber);

		assertEquals(123L, num);
	}

	@Test
	void testDecodeWhenTypeNumberAndZero() {
		String input = "i0e";

		Long num = bencode.decode(input, BencodeType.bencodeNumber);

		assertEquals(0L, num);
	}

	@Test
	void testDecodeWhenTypeNumberAndNegative() {
		String input = "i-250e";

		Long num = bencode.decode(input, BencodeType.bencodeNumber);

		assertEquals(-250L, num);
	}

	@Test
	void testDecodeWhenTypeString() {
		String input = "4:spam";

		String str = bencode.decode(input, BencodeType.bencodeString);

		assertEquals("spam", str);
	}

	@Test
	void testDecodeWhenTypeList() {
		String input = "l4:spam4:eggse";

		List<Object> list = bencode.decode(input, BencodeType.bencodeList);

		assertIterableEquals(List.of("spam", "eggs"), list);
	}

	@Test
	void testDecodeWhenTypeDictionary() {
		String input = "d3:cow3:moo4:spam4:eggse";

		Map<String, Object> list = bencode.decode(input, BencodeType.bencodeDictionary);

		assertEquals(Map.of("cow", "moo", "spam", "eggs"), list);
	}

	@Test
	void testEncodeLongWhenNullShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.encode((Long) null));
	}

	@Test
	void testEncodeStringWhenNullShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.encode((String) null));
	}

	@Test
	void testEncodeListWhenNullShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.encode((List<?>) null));
	}

	@Test
	void testEncodeMapWhenNullShouldThrow() {
		assertThrows(BencodeException.class, () -> bencode.encode((Map<?, ?>) null));
	}

	@Test
	void testEncodeLong() {
		byte[] encoded = bencode.encode(123L);

		assertEquals("i123e", new String(encoded));
	}

	@Test
	void testEncodeString() {
		byte[] encoded = bencode.encode("spam");

		assertEquals("4:spam", new String(encoded));
	}

	@Test
	void testEncodeList() {
		byte[] encoded = bencode.encode(List.of("spam", "eggs"));

		assertEquals("l4:spam4:eggse", new String(encoded));
	}

	@Test
	void testEncodeMap() {
		byte[] encoded = bencode.encode(Map.of("cow", "moo", "spam", "eggs"));

		assertEquals("d3:cow3:moo4:spam4:eggse", new String(encoded));
	}

}