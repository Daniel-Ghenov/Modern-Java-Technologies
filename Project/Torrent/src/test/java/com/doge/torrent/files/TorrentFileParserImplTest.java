package com.doge.torrent.files;

import com.doge.torrent.files.bencode.Bencode;
import com.doge.torrent.files.model.TorrentFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TorrentFileParserImplTest
{

	@Test
	void parse() {
		Bencode bencode = mock(Bencode.class);
		TorrentFileParserImpl parser = new TorrentFileParserImpl(bencode);


		Map<String, Object> response = Map.of(
				"info", Map.of(
						"length", 123L,
						"name", "name",
						"piece length", 123L,
						"pieces", "12345678901234567890"
				),
				"announce", "announce",
				"url-list", List.of("url1", "url2"),
				"announce-list" , List.of(List.of("announce1", "announce2"))
		);
		when(bencode.decode((byte[])any(), any())).thenReturn(response);
		TorrentFile torrentFile = parser.parse(new byte[]{});

		assertEquals("announce", torrentFile.announce());
		assertEquals(List.of("url1", "url2"), torrentFile.urlList());
		assertIterableEquals(List.of("announce1", "announce2"), torrentFile.announceList());
		assertEquals(123L, torrentFile.info().length());
		assertEquals("name", torrentFile.info().name());
		assertEquals(123L, torrentFile.info().pieceLength());
		assertEquals("12345678901234567890", new String(torrentFile.info().pieces().get(0).hash()));


	}

}