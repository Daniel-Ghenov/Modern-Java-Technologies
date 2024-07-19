package com.doge.tracker;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.TorrentFileParser;
import com.doge.tracker.model.PeerInsertion;
import com.doge.tracker.model.TorrentTrackerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TorrentTrackerTest
{

	TorrentTracker tracker;
	private static final Peer peer1 = new Peer("127.0.0.1", 6969, "PEER1");
	private static final Peer peer2 = new Peer("127.0.0.1", 6969, "PEER2");

	Map<String, List<PeerInsertion>> peersByInfoHash;
	@BeforeEach
	void setUp()
	{
		TorrentFileParser parser = mock(TorrentFileParser.class);

		this.peersByInfoHash = new ConcurrentHashMap<>(
				Map.of("infoHash", new ArrayList<>(List.of(new PeerInsertion(peer1, null), new PeerInsertion(peer2, null))))
		);
		this.tracker = new TorrentTracker(peersByInfoHash, parser);
	}

	@Test
	void testGetResponseForInfoHash() {
		TorrentTrackerResponse response = tracker.getResponseForInfoHash("infoHash");
		assertEquals(1800L, response.interval());
		assertIterableEquals(List.of(peer1, peer2), response.peers());
	}

	@Test
	void testGetPeersByInfoHash() {
		List<Peer> peers = tracker.getPeersByInfoHash("infoHash");
		assertIterableEquals(List.of(peer1, peer2), peers);
	}

	@Test
	void testAddPeer() {
		Peer newPeer = new Peer("127.0.0.1", 6969, "PEER3");
		tracker.addPeer("infoHash", newPeer);
		List<Peer> peers = peersByInfoHash.get("infoHash").stream().map(PeerInsertion::getPeer).toList();
		assertIterableEquals(List.of(peer1, peer2, newPeer), peers);
	}
}