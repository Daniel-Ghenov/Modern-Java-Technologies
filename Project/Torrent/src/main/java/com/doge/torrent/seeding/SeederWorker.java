package com.doge.torrent.seeding;

import com.doge.torrent.connection.message.Handshake;
import com.doge.torrent.connection.message.Message;
import com.doge.torrent.connection.message.MessageId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SeederWorker implements Runnable {
	private static final int INTEGER_BYTE_SIZE = 4;
	private final String peerId;
	private final Socket socket;
	private final TorrentSeeder seeder;
	private String infoHash;

	public SeederWorker(String peerId, Socket socket, TorrentSeeder seeder) {
		this.peerId = peerId;
		this.socket = socket;
		this.seeder = seeder;
	}

	@Override public void run() {
		getHandshake();
		boolean shouldExit = false;
		while (!shouldExit && !Thread.currentThread().isInterrupted()) {
			Message message = readMessage();
			respondToMessage(message);
		}
	}

	private void respondToMessage(Message message) {
		switch (message.id()) {
			case INTERESTED -> writeMessage(Message.CHOKE);
			case REQUEST -> returnPiece(message);
		}
	}

	private void returnPiece(Message message) {
		ByteBuffer buffer = ByteBuffer.wrap(message.payload());
		int index = buffer.getInt();
		int begin = buffer.getInt();
		int length = buffer.getInt();
		byte[] piece = seeder.getPiece(infoHash, index, begin, length);
		ByteBuffer response = ByteBuffer.allocate(INTEGER_BYTE_SIZE * 2 + length);
		response.putInt(index);
		response.putInt(begin);
		response.put(piece);
		writeMessage(new Message(MessageId.PIECE, response.array()));

	}

	private Message readMessage() {
		try {
			InputStream in = socket.getInputStream();
			byte[] lengthBytes = new byte[INTEGER_BYTE_SIZE];
			in.read(lengthBytes);
			int length = ByteBuffer.wrap(lengthBytes).getInt();

			if (length == 0) {
				return Message.KEEP_ALIVE;
			} else if (length < 0) {
				throw new IllegalArgumentException("Invalid message length: " + length);
			}

			byte[] payload = new byte[length];
			in.read(payload);

			byte[] message = new byte[length + INTEGER_BYTE_SIZE];
			System.arraycopy(lengthBytes, 0, message, 0, INTEGER_BYTE_SIZE);
			System.arraycopy(payload, 0, message, INTEGER_BYTE_SIZE, length);

			return Message.fromBytes(message);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void getHandshake() {
		try {
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			byte[] clientHandshake = new byte[Handshake.HANDSHAKE_LENGTH];
			in.read(clientHandshake);
			Handshake handshakeMessage = Handshake.fromMessage(clientHandshake);
			this.infoHash = handshakeMessage.infoHash();
			if (!seeder.hasInfoHash(infoHash)) {
				socket.close();
			}

			Handshake handshake = new Handshake(infoHash, peerId);
			byte[] message = handshake.toMessage();
			out.write(message);
			out.write(seeder.getBitField(infoHash).toBytes());
			writeMessage(Message.UNCHOKE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMessage(Message message) {
		try {
			OutputStream out = socket.getOutputStream();
			out.write(message.toBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
