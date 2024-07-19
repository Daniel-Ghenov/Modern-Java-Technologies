package com.doge.torrent.connection;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.connection.message.BitField;
import com.doge.torrent.connection.message.Message;
import com.doge.torrent.connection.message.MessageId;
import com.doge.torrent.connection.piece.PieceProgress;
import com.doge.torrent.files.model.TorrentPiece;
import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class ClientWorker implements Runnable {

	private final BlockingQueue<TorrentPiece> pieceQueue;
	private final BlockingQueue<PieceProgress> finishedQueue;
	private final ClientConnector connector;
	private final Peer peer;
	private BitField bitField;
	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(ClientWorker.class);
	private static final int SECOND_IN_MILLIS = 1000;
	public ClientWorker(
			BlockingQueue<TorrentPiece> pieceQueue,
			BlockingQueue<PieceProgress> finishedQueue,
			ClientConnector connector,
			Peer peer) {
		this.pieceQueue = pieceQueue;
		this.finishedQueue = finishedQueue;
		this.connector = connector;
		this.peer = peer;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Started download for peer: " + peer);
			connector.connect(peer);
			LOGGER.info("Sent interested to peer: " + peer);
			readBitfield();
			if (bitField == null || bitField.isEmpty()) {
				return;
			}
		} catch (Exception e) {
			LOGGER.error("Error while connecting to peer: " + peer, e);
			connector.disconnect();
			return;
		}
		connector.sendMessage(Message.INTERESTED);
		Message message = connector.readMessage();
		while (message.id() != MessageId.UNCHOKE) {
			message = connector.readMessage();
		}
		while (!Thread.currentThread().isInterrupted() &&
			   !pieceQueue.isEmpty() &&
			   !connector.isDisconnected()) {
			tryToDownloadPiece();
		}
	}

	private void tryToDownloadPiece() {
		try {
			TorrentPiece piece = pieceQueue.take();
			LOGGER.info("Checking piece: " + piece + " for peer: " + peer);
			if (bitField.hasPiece(piece.index())) {
				LOGGER.info("Peer: " + peer + " has piece: " + piece + "starting download");
				PieceProgress downloaded = connector.downloadPiece(piece);
				if (downloaded.isComplete()) {
					LOGGER.info("Downloaded piece: " + piece + " from peer: " + peer);
					finishedQueue.put(downloaded);
				} else {
					pieceQueue.put(piece);
				}
			} else {
				LOGGER.info("Peer: " + peer + " does not have piece: " + piece);
				pieceQueue.put(piece);
			}
		} catch (InterruptedException e) {
			LOGGER.error("Error while downloading piece from peer: " + peer, e);
			Thread.currentThread().interrupt();
			connector.disconnect();
		} catch (Exception e) {
			LOGGER.error("Error while downloading piece from peer: " + peer, e);
			connector.disconnect();
		}
	}

	private void readBitfield() {
		Message message = connector.readMessage();
		if (isValidMessage(message)) {
			LOGGER.debug("Received message from peer: " + peer + "id: " + message.id()
					+ " payload: " + new String(message.payload(), StandardCharsets.ISO_8859_1));
			if (message.id() == MessageId.BITFIELD) {
				LOGGER.info("Received bitfield " +
							new String(message.payload(), StandardCharsets.ISO_8859_1) +
							" from peer: " + peer);
				bitField = new BitField(message.payload());
				if (bitField.isEmpty()) {
					LOGGER.info("Peer: " + peer + " does not have any pieces");
					connector.disconnect();
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private boolean isValidMessage(Message message) {
		return message != null && !message.isKeepAlive();
	}
}
