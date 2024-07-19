package com.doge.torrent.connection.message;

import static com.doge.torrent.utils.ByteUtils.toByte;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public record Handshake(
		String infoHash,
		String peerId
) {
	public static final int HANDSHAKE_LENGTH = 68;
	private static final int PROTOCOL_IDENTIFIER_LENGTH = 19;
	private static final int PROTOCOL_ID_LENGTH_SIZE = 1;
	private static final int RESERVED_BYTES_LENGTH = 8;
	private static final int INFO_HASH_LENGTH = 20;
	private static final int PEER_ID_LENGTH = 20;
	private static final byte[] RESERVED_BYTES = new byte[RESERVED_BYTES_LENGTH];
	private static final byte[] PROTOCOL_IDENTIFIER = "BitTorrent protocol".getBytes();

	public byte[] toMessage() {
		byte[] message = new byte[HANDSHAKE_LENGTH];
		byte[] protocolIdentifierLength = new byte[1];
		protocolIdentifierLength[0] = toByte(PROTOCOL_IDENTIFIER_LENGTH);

		System.arraycopy(protocolIdentifierLength, 0, message, 0, 1);
		System.arraycopy(PROTOCOL_IDENTIFIER, 0, message, PROTOCOL_ID_LENGTH_SIZE, PROTOCOL_IDENTIFIER_LENGTH);
		System.arraycopy(RESERVED_BYTES, 0, message, PROTOCOL_ID_LENGTH_SIZE +
						 PROTOCOL_IDENTIFIER_LENGTH,
						 RESERVED_BYTES_LENGTH);

		System.arraycopy(infoHash.getBytes(DEFAULT_CHARSET), 0, message,
						 PROTOCOL_IDENTIFIER_LENGTH +
						 RESERVED_BYTES_LENGTH + PROTOCOL_ID_LENGTH_SIZE, INFO_HASH_LENGTH);

		System.arraycopy(peerId.getBytes(DEFAULT_CHARSET), 0, message,
						 PROTOCOL_IDENTIFIER_LENGTH +
						 RESERVED_BYTES_LENGTH + INFO_HASH_LENGTH +
						 PROTOCOL_ID_LENGTH_SIZE,
						 PEER_ID_LENGTH);
		return message;
	}

	public static Handshake fromMessage(byte[] message) {

		String infoHash = new String(message,
									 PROTOCOL_IDENTIFIER_LENGTH +
									 PROTOCOL_ID_LENGTH_SIZE +
									 RESERVED_BYTES_LENGTH, INFO_HASH_LENGTH,
									 DEFAULT_CHARSET);

		String peerId = new String(message,
								   PROTOCOL_IDENTIFIER_LENGTH +
								   PROTOCOL_ID_LENGTH_SIZE +
								   RESERVED_BYTES_LENGTH + INFO_HASH_LENGTH,
								   INFO_HASH_LENGTH,
								   DEFAULT_CHARSET);

		return new Handshake(infoHash, peerId);
	}

	public void validatePeerHandshake(Handshake clientHandshake) {
		if (!infoHash.equals(clientHandshake.infoHash())) {
			throw new HandshakeException("Info hash does not match");
		}
	}
}
