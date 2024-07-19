package com.doge.torrent.announce;

import com.doge.torrent.announce.model.AnnounceRequestBuilder;
import com.doge.torrent.announce.model.AnnounceResponse;
import com.doge.torrent.announce.model.Event;
import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.bencode.Bencode;
import com.doge.torrent.stubs.HttpResponseStub;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.doge.torrent.files.bencode.BencodeType.bencodeDictionary;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnouncerImplTest
{

	@Test
	void testWhenAnnounceWithPeerMapShouldReturnValidResponse() {
		HttpClient client = mock(HttpClient.class);
		Bencode bencode = mock(Bencode.class);

		Announcer announcer = new AnnouncerImpl(client, bencode);
		Long interval = 1800L;
		Peer peer = new Peer("127.0.0.1", 6881, "12345678901234567890");

		Map<String, Object> returnedResponse = Map.of(
				"interval", interval,
				"peers", List.of(
						Map.of(
								"peer id", peer.peerId(),
								"ip", peer.address().getAddress().getHostName(),
								"port", (long) peer.address().getPort())
						)
		);

		try
		{
			when(bencode.decode((byte[]) any(), any()))
					.thenReturn(returnedResponse);
			when(client.send(any(), any()))
					.thenReturn(new HttpResponseStub<>(new byte[]{0}, 200));

			AnnounceResponse response = announcer.announce(
					AnnounceRequestBuilder.fromUrl("http://www.google.com")
							.peerId("12345678901234567890")
							.infoHash("Hash")
							.left(0L)
							.downloaded(0L)
							.uploaded(0L)
							.event(Event.STARTED)
						    .build());

			assertEquals(interval, response.interval());
			assertIterableEquals(List.of(peer), response.peers());

		}
			catch (IOException | InterruptedException e)
			{
				fail("Unexpected exception: " + e.getMessage());
			}
	}

	@Test
	void testWhenAnnounceWithPeerStringShouldReturnCorrectValue() {
		HttpClient client = mock(HttpClient.class);
		Bencode bencode = mock(Bencode.class);

		Announcer announcer = new AnnouncerImpl(client, bencode);
		Long interval = 1800L;
		Peer peer = new Peer("127.0.0.1", 6881, "12345678901234567890");

		Map<String, Object> returnedResponse = Map.of(
				"interval", interval,
				"peers", new String(new byte[]{127, 0, 0, 1, 26, -31}, StandardCharsets.ISO_8859_1)
						 + "12345678901234567890"
		 );

		try
		{
			when(bencode.decode((byte[]) any(), any()))
					.thenReturn(returnedResponse);
			when(client.send(any(), any()))
					.thenReturn(new HttpResponseStub<>(new byte[]{0}, 200));

			AnnounceResponse response = announcer.announce(
					AnnounceRequestBuilder.fromUrl("http://www.google.com")
										  .build());

			assertEquals(interval, response.interval());
			assertIterableEquals(List.of(peer), response.peers());

		}
		catch (IOException | InterruptedException e)
		{
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test
	void announceResponseFromMap()
	{
	}
}