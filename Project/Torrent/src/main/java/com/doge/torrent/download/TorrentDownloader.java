package com.doge.torrent.download;

import com.doge.torrent.announce.Announcer;
import com.doge.torrent.announce.model.AnnounceRequest;
import com.doge.torrent.announce.model.AnnounceRequestBuilder;
import com.doge.torrent.announce.model.AnnounceResponse;
import com.doge.torrent.announce.model.Event;
import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.connection.ClientConnector;
import com.doge.torrent.connection.ClientWorker;
import com.doge.torrent.connection.TCPClientConnector;
import com.doge.torrent.connection.piece.PieceProgress;
import com.doge.torrent.download.files.FileTorrentSaver;
import com.doge.torrent.download.files.TorrentSaver;
import com.doge.torrent.files.TorrentFileParser;
import com.doge.torrent.files.model.TorrentFile;
import com.doge.torrent.files.model.TorrentPiece;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;
import com.doge.torrent.seeding.TorrentSeeder;
import com.doge.torrent.seeding.TorrentSeederServer;

public class TorrentDownloader {

	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(TorrentDownloader.class);
	private static final String DEFAULT_PEER_ID = "DOGE-TORRENT";
	public static final Integer DEFAULT_PORT = 50421;
	private static final int PEER_ID_INT_MIN_NUMBER = 1_000_000;
	private static final int PEER_ID_INT_MAX_NUMBER = 9_999_999;
	private static final int WAIT_TIME = 100;
	private final TorrentFileParser parser;
	private final ExecutorService executorService;
	private final Announcer announcer;
	private final String peerId;
	private Map<String, List<Peer>> peersByInfoHash = new ConcurrentHashMap<>();
	private Map<String, Map<Integer, PieceProgress>> piecesByIndexByInfoHash;
	private Map<String, Integer> pieceCountByInfoHash = new ConcurrentHashMap<>();

	public TorrentDownloader(TorrentFileParser parser, Announcer announcer) {
		this.parser = parser;
		this.announcer = announcer;
		this.executorService = Executors.newVirtualThreadPerTaskExecutor();
		this.peerId = generatePeerId();
		piecesByIndexByInfoHash = new ConcurrentHashMap<>();

		TorrentSeeder seeder = new TorrentSeeder(piecesByIndexByInfoHash, pieceCountByInfoHash);
		TorrentSeederServer seederServer = new TorrentSeederServer(seeder, this.peerId, DEFAULT_PORT);
		Thread thread = new Thread(seederServer::start);
		executorService.submit(thread);
	}

	private String generatePeerId() {
		Random random = new Random();
		int peerIdInt = random.nextInt(PEER_ID_INT_MAX_NUMBER - PEER_ID_INT_MIN_NUMBER);
		peerIdInt += PEER_ID_INT_MIN_NUMBER;
		return DEFAULT_PEER_ID + "-" + peerIdInt;
	}

	public void download(String path) {
		TorrentFile file = getTorrentFile(path);

		piecesByIndexByInfoHash.put(file.infoHash(), new ConcurrentHashMap<>());

		BlockingQueue<PieceProgress> finishedQueue = new LinkedBlockingQueue<>();
		BlockingQueue<TorrentPiece> pieceQueue = new LinkedBlockingQueue<>(file.info().pieces());

		TorrentSaver saver = new FileTorrentSaver(getPath(path, file));

		peersByInfoHash.get(file.infoHash())
				   .forEach(peer -> runDownloadForPeer(finishedQueue, pieceQueue, file, peer));
		pieceCountByInfoHash.put(file.infoHash(), file.info().pieces().size());
		AtomicBoolean hasFinished = new AtomicBoolean(false);
		runDownloadedWorker(finishedQueue, hasFinished, saver, file, file.info().pieces().size());
		while (!hasFinished.get()) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				LOGGER.error("Error while waiting for download to finish", e);
			}
		}
	}

	private TorrentFile getTorrentFile(String path) {
		TorrentFile file = parser.parseFromPath(path);
		AnnounceRequest request = AnnounceRequestBuilder.fromUrl(file.getAnnounceUrl())
			.infoHash(file.infoHash())
			.peerId(peerId)
			.left(file.info().length())
			.event(Event.STARTED)
			.port(DEFAULT_PORT)
			.build();

		AnnounceResponse response = announcer.announce(request);
		LOGGER.info("Announce response: " + response);

		peersByInfoHash.put(file.infoHash(), response.peers());
		LOGGER.info("Peers: " + response.peers());
		return file;
	}

	private static String getPath(String path, TorrentFile file) {
		String separator = System.getProperty("file.separator");
		int lastSeperator = path.lastIndexOf(separator);
		String newPath = path.substring(0, lastSeperator + 1) + file.info().name();
		LOGGER.info("File path: " + newPath);
		return newPath;
	}

	private void runDownloadedWorker(BlockingQueue<PieceProgress> finishedQueue,
									 AtomicBoolean hasFinished,
									 TorrentSaver saver,
									 TorrentFile file,
									 int pieceCount) {

		DownloadedWorker worker = new DownloadedWorker(finishedQueue,
									piecesByIndexByInfoHash.get(file.infoHash()),
									hasFinished,
									saver, file.info().length(), pieceCount);
		executorService.submit(worker);
		LOGGER.info("Started downloaded worker");

	}

	private void runDownloadForPeer(BlockingQueue<PieceProgress> finishedQueue,
									BlockingQueue<TorrentPiece> pieceQueue,
									TorrentFile file,
									Peer peer) {
//		//TODO: remove this temporary check
//		if (peer.peerId() != null && peer.peerId().contains("DOGE")) {
//			return;
//		}
		ClientConnector connector = new TCPClientConnector(file.infoHash(), peerId);
		ClientWorker worker = new ClientWorker(pieceQueue, finishedQueue, connector, peer);
		executorService.submit(worker);

	}
}
