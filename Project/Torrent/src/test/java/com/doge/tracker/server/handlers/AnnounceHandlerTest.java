package com.doge.tracker.server.handlers;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.bencode.TorrentEncoder;
import com.doge.torrent.logging.Level;
import com.doge.torrent.logging.TorrentLoggerFactory;
import com.doge.torrent.utils.URIBuilder;
import com.doge.tracker.TorrentTracker;
import com.doge.tracker.model.TorrentTrackerResponse;
import com.doge.tracker.stubs.HttpExchangeStub;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnnounceHandlerTest
{

	@BeforeAll
	static void removeLogging() {
		TorrentLoggerFactory.setLevel(Level.OFF);
	}

	@ParameterizedTest(name = "Test handle when method is {0}")
	@ValueSource(strings = {"HEAD", "PUT", "DELETE", "OPTIONS", "TRACE", "CONNECT"})
	void testHandleWhenMethodNotGet(String method) {
		TorrentTracker tracker = mock(TorrentTracker.class);
		TorrentEncoder encoder = mock(TorrentEncoder.class);
		AnnounceHandler announceHandler = new AnnounceHandler(tracker, encoder);

		HttpExchange exchange = new HttpExchangeStub(method, null, null, null);

		try
		{
			announceHandler.handle(exchange);
		}
		catch (IOException e)
		{
			fail("Unexpected IOException thrown");
		}

		assertEquals(400, exchange.getResponseCode());
	}

	@Test
	void testGetPeersWhenCorrectInfoHash() {
		TorrentTracker tracker = mock(TorrentTracker.class);
		TorrentEncoder encoder = mock(TorrentEncoder.class);
		AnnounceHandler announceHandler = new AnnounceHandler(tracker, encoder);

		String infoHash = "12345678901234567890";

		URI uri = URIBuilder.fromURL("http://localhost:6969/announce?info_hash=" + infoHash + "&peer_id=ABCDEFGHIJKLMNOPQRST&port=6881&uploaded=0&downloaded=0&left=0&event=started")
				.buildURI();
		ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
		HttpExchange exchange = new HttpExchangeStub("GET", uri, new InetSocketAddress("127.0.0.1", 34567), responseBody);
		Peer currentPeer = new Peer("127.0.0.1", 6969, "PEER_ID");
		Peer newPeer = new Peer("127.0.0.1", 6881, "ABCDEFGHIJKLMNOPQRST");
		when(tracker.getResponseForInfoHash(infoHash)).thenReturn(new TorrentTrackerResponse(1800L, List.of(currentPeer)));

		String encodedResponse = "d8:intervali1800e5:peersld2:ip9:127.0.0.17:peer id7:PEER_ID:porti6881eeee";
		when(encoder.encode((Map) any())).thenReturn(encodedResponse.getBytes());

		try {
			announceHandler.handle(exchange);
			assertEquals(200, exchange.getResponseCode());
			assertEquals(encodedResponse, responseBody.toString());
			verify(tracker).addPeer(infoHash, newPeer);
		} catch (IOException e) {
			fail("Unexpected IOException thrown");
		}
	}

}