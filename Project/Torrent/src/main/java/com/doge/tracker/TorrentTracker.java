package com.doge.tracker;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.TorrentFileParser;
import com.doge.torrent.files.model.TorrentFile;
import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;
import com.doge.tracker.cleanup.CleanupWorker;
import com.doge.tracker.exception.NoInfoHashPresentException;
import com.doge.tracker.model.PeerInsertion;
import com.doge.tracker.model.TorrentTrackerResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TorrentTracker {
	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(TorrentTracker.class);

	private static final Long DEFAULT_INTERVAL = 1800L;

	private static final Long PEER_DELETION_INTERVAL = 240_000L;

	private final Map<String, List<PeerInsertion>> peersByInfoHash;

	private final TorrentFileParser torrentFileParser;

	public TorrentTracker(TorrentFileParser torrentFileParser) {
		this(new ConcurrentHashMap<>(), torrentFileParser);
	}

	public TorrentTracker(Map<String, List<PeerInsertion>> peersByInfoHash, TorrentFileParser torrentFileParser) {
		this.peersByInfoHash = peersByInfoHash;
		this.torrentFileParser = torrentFileParser;
		startCleanupWorker();
	}

	public TorrentTrackerResponse getResponseForInfoHash(String infoHash) {
		return new TorrentTrackerResponse(DEFAULT_INTERVAL, getPeersByInfoHash(infoHash));
	}

	public List<Peer> getPeersByInfoHash(String infoHash) {
		if (!peersByInfoHash.containsKey(infoHash)) {
			LOGGER.error("No such infoHash: " + infoHash);
			throw new NoInfoHashPresentException("No such infoHash: " + infoHash);
		}
		return peersByInfoHash.get(infoHash)
				.stream()
				.map(PeerInsertion::getPeer)
				.collect(Collectors.toList());

	}

	public void addPeer(String infoHash, Peer peer) {
		if (!peersByInfoHash.containsKey(infoHash)) {
			LOGGER.error("No such infoHash: " + infoHash);
			throw new NoInfoHashPresentException("No such infoHash: " + infoHash);
		}

		peersByInfoHash.get(infoHash)
				   .removeIf(peerInsertion -> peerInsertion.getPeer().peerId().equals(peer.peerId()));
		peersByInfoHash.get(infoHash).add(new PeerInsertion(peer, LocalDateTime.now()));
	}

	private void startCleanupWorker() {
		new Thread(new CleanupWorker<>(PEER_DELETION_INTERVAL, peersByInfoHash)).start();
	}

	public void loadTorrents(String path) {
		List<TorrentFile> files = torrentFileParser.parseAllFromPath(path);
		files.forEach(this::loadFile);
	}

	private void loadFile(TorrentFile torrentFile) {
		LOGGER.debug("Loaded torrent: " + torrentFile);
		peersByInfoHash.putIfAbsent(torrentFile.infoHash(), Collections.synchronizedList(new ArrayList<>()));
	}
}
