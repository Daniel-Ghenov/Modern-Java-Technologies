package com.doge.torrent.connection;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.connection.exception.ClientConnectionException;
import com.doge.torrent.connection.message.Handshake;
import com.doge.torrent.connection.message.Message;
import com.doge.torrent.connection.piece.PieceProgress;
import com.doge.torrent.files.model.TorrentPiece;
import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.doge.torrent.files.hasher.TorrentHasher.hash;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class TCPClientConnector implements ClientConnector {
	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(TCPClientConnector.class);
	private static final int MAX_BLOCK_SIZE = 16384;
	private static final int INT_BYTE_SIZE = 4;
	private boolean disconnected = true;
	private Peer peer;
	private final String infoHash;
	private final String peerId;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	public TCPClientConnector(String infoHash, String peerId) {
		this.infoHash = infoHash;
		this.peerId = peerId;
	}

	@Override
	public void connect(Peer peer) {
		this.peer = peer;
		try  {
			Socket socket = new Socket(peer.address().getAddress(), peer.address().getPort());
			this.socket = socket;
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();

			Handshake handshake = new Handshake(infoHash, peerId);
			byte[] message = handshake.toMessage();
			LOGGER.debug("Sending handshake to peer: " + peer + "Handshake: " +
						new String(message, StandardCharsets.ISO_8859_1));
			out.write(message);
			byte[] response = new byte[Handshake.HANDSHAKE_LENGTH];
			int bytesRead = in.read(response);

			if (bytesRead != Handshake.HANDSHAKE_LENGTH) {
				throw new ClientConnectionException("Invalid handshake length: " + bytesRead);
			}
			Handshake responseHandshake = Handshake.fromMessage(response);
			LOGGER.debug("Received handshake from peer: " + peer + "Handshake: " + responseHandshake);
			responseHandshake.validatePeerHandshake(handshake);
			disconnected = false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void disconnect() {
		if (socket == null) {
			return;
		}
		try {
			socket.close();
			in.close();
			out.close();
			disconnected = true;
			LOGGER.info("Disconnected from peer: " + peer);
		} catch (IOException e) {
			LOGGER.error("Failed to close socket");
		}
	}

	@Override
	public PieceProgress downloadPiece(TorrentPiece piece) {
		PieceProgress progress = new PieceProgress(piece);
		boolean requestedPiece = false;
		while (!progress.isComplete()) {
			Message message = Message.request(piece.index(), progress.requested(),
				  Math.min(MAX_BLOCK_SIZE, (int) piece.pieceLength() - progress.requested()));
			if (!requestedPiece) {
				sendMessage(message);
				requestedPiece = true;
			}
			Message response = readMessage();
			if (response.isPiece()) {
				progress.addBlock(response);
				requestedPiece = false;
			}
		}
		if (!validatePiece(progress)) {
			throw new ClientConnectionException("Error downloading piece. Invalid hash.");
		}
		return progress;
	}

	private boolean validatePiece(PieceProgress progress) {
		byte[] hash = hash(progress.data()).getBytes(DEFAULT_CHARSET);
		if (!Arrays.equals(progress.hash(), hash)) {
			LOGGER.error("Piece hash does not match");
			return false;
		}
		return true;
	}

	@Override
	public Message readMessage() {
		try {
			byte[] lengthBytes = new byte[INT_BYTE_SIZE];
			int bytesRead = in.read(lengthBytes);
			int length = ByteBuffer.wrap(lengthBytes).getInt();
			if (length == -1) {
				throw new ClientConnectionException("Connection closed");
			}
			if (length < -1) {
				return Message.KEEP_ALIVE;
			}
			byte[] bytes = new byte[length];
			in.read(bytes);
			if (length == 0) {
				return Message.KEEP_ALIVE;
			}
			byte[] messageBytes = new byte[length + INT_BYTE_SIZE];
			ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
			buffer.putInt(length);
			buffer.put(bytes);
			return Message.fromBytes(messageBytes);
		} catch (IOException e) {
			throw new ClientConnectionException(e);
		}
	}

	@Override public boolean isDisconnected() {
		return disconnected;
	}

	@Override
	public void sendMessage(Message message) {
		try {
			out.write(message.toBytes());
		} catch (IOException e) {
			throw new ClientConnectionException(e);
		}
	}
}
