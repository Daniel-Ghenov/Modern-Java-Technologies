package com.doge.tracker.server.handlers;

import com.doge.torrent.announce.exception.AnnouncementError;
import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.bencode.TorrentEncoder;
import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;
import com.doge.tracker.TorrentTracker;
import com.doge.tracker.exception.NoInfoHashPresentException;
import com.doge.tracker.model.TorrentTrackerResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.doge.torrent.announce.exception.AnnouncementError.INVALID_INFO_HASH;
import static com.doge.torrent.announce.exception.AnnouncementError.INVALID_PEER_ID;

public class AnnounceHandler implements HttpHandler {

	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(AnnounceHandler.class);

	private final TorrentTracker torrentTracker;

	private final TorrentEncoder torrentEncoder;

	private static final int BAD_REQUEST = 400;

	private static final int OK = 200;

	private static final String FAILURE_REASON_KEY = "failure reason";

	public AnnounceHandler(TorrentTracker torrentTracker, TorrentEncoder torrentEncoder) {
		this.torrentTracker = torrentTracker;
		this.torrentEncoder = torrentEncoder;
	}

	@Override public void handle(HttpExchange exchange) throws IOException {
		LOGGER.debug("Handling announce request");
		if (!exchange.getRequestMethod().equals("GET")) {
			LOGGER.error("Invalid request method: " + exchange.getRequestMethod());
			exchange.sendResponseHeaders(BAD_REQUEST, -1);
			return;
		}
		Map<String, String> queryParams = extractQueryParams(exchange.getRequestURI().getRawQuery());
		byte[] response = getResponse(queryParams);
		LOGGER.debug("Got response for infoHash: " + new String(response));
		AnnouncementError err = savePeer(queryParams, exchange.getRemoteAddress());

		if (err != null) {
			LOGGER.error("Error saving peer: " + err.getMessage());
			response = torrentEncoder.encode(Map.of(FAILURE_REASON_KEY, err.getMessage()));
		}

		exchange.sendResponseHeaders(OK, response.length);
		exchange.getResponseBody().write(response);
	}

	private AnnouncementError savePeer(Map<String, String> queryParams, InetSocketAddress remoteAddress) {
		String peerId = queryParams.get("peer_id");
		if (peerId == null) {
			return INVALID_PEER_ID;
		}

		String infoHash = queryParams.get("info_hash");
		if (infoHash == null) {
			return AnnouncementError.MISSING_INFO_HASH;
		}
		infoHash = URLDecoder.decode(infoHash, StandardCharsets.ISO_8859_1);

		String port = queryParams.get("port");
		if (port == null) {
			return AnnouncementError.MISSING_PORT;
		}
		try {
			Peer peer = new Peer(remoteAddress.getHostName(), Integer.parseInt(port), peerId);
			torrentTracker.addPeer(infoHash, peer);
		} catch (NoInfoHashPresentException e) {
			return INVALID_INFO_HASH;
		}
		return null;
	}

	private byte[] getResponse(Map<String, String> queryParams) {
		try	{
			String infoHash = queryParams.get("info_hash");
			TorrentTrackerResponse response = torrentTracker.getResponseForInfoHash(infoHash);
			Map<String, Object> responseMap = new HashMap<>();

			responseMap.put("interval", response.interval());
			List<Map<String, Object>> peers = response.peers().stream().map(this::peerToMap).toList();
			responseMap.put("peers", peers);

			return torrentEncoder.encode(responseMap);
		} catch (NoInfoHashPresentException e) {
			return torrentEncoder.encode(Map.of(FAILURE_REASON_KEY, INVALID_INFO_HASH.getMessage()));
		}
	}

	private Map<String, Object> peerToMap(Peer peer) {
		Map<String, Object> peerMap = new HashMap<>();
		peerMap.put("ip", peer.address().getHostName());
		peerMap.put("port", (long) peer.address().getPort());
		peerMap.put("peer id", peer.peerId());
		return peerMap;
	}

	private Map<String, String> extractQueryParams(String query) {
		Map<String , String> queryParams = new HashMap<>();
		Arrays.stream(query.split("&" ))
				.map(param -> param.split("="))
				.forEach(param -> queryParams.put(param[0],
		  			URLDecoder.decode(param[1], StandardCharsets.ISO_8859_1)));
		return queryParams;
	}
}
