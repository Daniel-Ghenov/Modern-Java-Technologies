package com.doge.torrent.announce;

import com.doge.torrent.announce.exception.AnnouncementError;
import com.doge.torrent.announce.exception.AnnouncementException;
import com.doge.torrent.announce.model.AnnounceRequest;
import com.doge.torrent.announce.model.AnnounceRequestBuilder;
import com.doge.torrent.announce.model.AnnounceResponse;
import com.doge.torrent.announce.model.Event;
import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.files.bencode.Bencode;
import com.doge.torrent.files.bencode.TorrentDecoder;
import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;
import com.doge.torrent.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.doge.torrent.files.bencode.BencodeType.bencodeDictionary;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class AnnouncerImpl implements Announcer {

	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(AnnouncerImpl.class);


	private static final int MIN_SUCCESS_STATUS_CODE = 200;
	private static final int MAX_SUCCESS_STATUS_CODE = 299;

	private final HttpClient httpClient;

	private final TorrentDecoder torrentDecoder;

	public AnnouncerImpl() {
		this(HttpClient.newHttpClient(), new Bencode());
	}

	public AnnouncerImpl(HttpClient httpClient, TorrentDecoder torrentDecoder) {
		this.httpClient = httpClient;
		this.torrentDecoder = torrentDecoder;
	}

	@Override
	public AnnounceResponse announce(AnnounceRequest request) {
		try {
			URI uri = buildUri(request);
			HttpRequest httpRequest = HttpRequest.newBuilder(uri)
												 .GET()
												 .build();
			return getAnnounceResponse(httpRequest);
		} catch (AnnouncementException e) {

			if (e.getError() == AnnouncementError.PEER_NOT_FOUND) {
				AnnounceRequest startedRequest = AnnounceRequestBuilder.fromAnnouncementRequest(request)
												   .event(Event.STARTED)
												   .build();
				return announce(startedRequest);
			}
			throw e;
		}
	}

	private AnnounceResponse getAnnounceResponse(HttpRequest httpRequest) {
		LOGGER.debug("Sending request: " + httpRequest);
		try {
			HttpResponse<byte[]> response = httpClient.send(httpRequest,
					HttpResponse.BodyHandlers.ofByteArray());
		    validateStatusCode(response.statusCode());
			return parseResponse(response.body());

		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void validateStatusCode(int statusCode) {
		if (statusCode < MIN_SUCCESS_STATUS_CODE ||
			statusCode > MAX_SUCCESS_STATUS_CODE) {
			throw new RuntimeException("Invalid status code: " + statusCode);
		}
	}

	private AnnounceResponse parseResponse(byte[] body) {
		Map<String, Object> decoded = torrentDecoder.decode(body, bencodeDictionary);
		LOGGER.debug("Decoded response: " + decoded);
		if (decoded.containsKey("failure reason")) {
			throw new AnnouncementException(decoded.get("failure reason").toString());
		}
		return announceResponseFromMap(decoded);
	}

	@SuppressWarnings("unchecked")
	public static AnnounceResponse announceResponseFromMap(Map<String, Object> map) {
		try {
			Long interval = (Long) map.get("interval");
			List<Peer> peers = getPeersFromDictionary(map.get("peers"));
			if (peers == null || peers.isEmpty()) {
				peers = getPeersFromString((String) map.get("peers"));
			}
			return new AnnounceResponse(interval, peers);
		} catch (ClassCastException e) {
			LOGGER.warn("Invalid map: " + map);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Peer> getPeersFromDictionary(Object peers) {
		if (peers == null) {
			return null;
		}
		if (peers instanceof List) {
			if ( ((List<?>) peers).isEmpty()) {
				return List.of();
			}

			return ((List<Map<String, Object>>) peers).stream()
					.map(AnnouncerImpl::getPeerFromMap)
					.toList();
		}
		return null;
	}

	private static Peer getPeerFromMap(Map<String, Object> map) {
		String ip = (String) map.get("ip");
		//TODO: remove this temporary check
		if (ip.equals("92.247.249.116")) {
			ip = "127.0.0.1";
		}
		Integer port = ((Long) map.get("port")).intValue();
		String peerId = (String) map.get("peer id");
		return new Peer(ip, port, peerId);
	}

	private static List<Peer> getPeersFromString(String peersString) {
		try {
			return getPeersWithByteLength(peersString, Peer.PEER_BYTE_LENGTH_WITH_ID);
		} catch (IllegalArgumentException e) {
			return getPeersWithByteLength(peersString, Peer.PEER_BYTE_LENGTH_NO_ID);
		}
	}

	private static List<Peer> getPeersWithByteLength(String peersString, int byteLength) {
		if (peersString.length() % byteLength != 0) {
			throw new IllegalArgumentException("Invalid peer string length: " + peersString.length());
		}
		List<Peer> peers = new ArrayList<>();
		for (int i = 0; i < peersString.length(); i += byteLength) {
			int end = Math.min(peersString.length(), i + byteLength);
			peers.add(Peer.fromByteArr(peersString.substring(i, end).getBytes(DEFAULT_CHARSET)));
		}
		return peers;
	}

	private URI buildUri(AnnounceRequest request) {
		LOGGER.info("Building URI for request: " + request);
		return URIBuilder.fromURL(request.trackerAnnounceUrl())
						 .queryParam("info_hash", request.infoHash(),
									 StandardCharsets.ISO_8859_1)
						 .queryParam("peer_id", request.peerId(),
									 StandardCharsets.ISO_8859_1)
						 .queryParam("downloaded", request.downloaded())
						 .queryParam("uploaded", request.uploaded())
						 .queryParam("left", request.left())
						 .queryParam("compact", request.compact())
						 .queryParam("port", request.port())
						 .queryParam("event", request.event().value())
						 .buildURI();
	}
}
