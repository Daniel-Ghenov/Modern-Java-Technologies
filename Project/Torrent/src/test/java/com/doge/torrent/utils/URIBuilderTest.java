package com.doge.torrent.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class URIBuilderTest {

	@Test
	void testBuildOnlyURL() {
		String url = "http://localhost:8080/announce";
		URIBuilder builder = URIBuilder.fromURL(url);
		assertEquals(url, builder.build());

	}

	@Test
	void testBuildURLWithOneQueryParam() {
		String url = "http://localhost:8080/announce";
		URIBuilder builder = URIBuilder.fromURL(url);
		builder.queryParam("info_hash", "12345678901234567890");
		assertEquals(url + "?info_hash=12345678901234567890", builder.build());
	}

	@Test
	void testBuildURLWithTwoQueryParams() {
		String url = "http://localhost:8080/announce";
		URIBuilder builder = URIBuilder.fromURL(url);
		builder.queryParam("info_hash", "12345678901234567890");
		builder.queryParam("peer_id", "12345678901234567890");
		assertEquals(url + "?info_hash=12345678901234567890&peer_id=12345678901234567890", builder.build());
	}

	@Test
	void testBuildUrlWithIterableParam() {
		String url = "http://localhost:8080/announce";
		List<String> peers = List.of("peer1", "peer2", "peer3");
		URIBuilder builder = URIBuilder.fromURL(url);
		builder.queryParam("peers", peers);
		assertEquals(url + "?peers=peer1&peers=peer2&peers=peer3", builder.build());
	}

}