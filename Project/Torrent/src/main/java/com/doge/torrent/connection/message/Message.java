package com.doge.torrent.connection.message;

import java.nio.ByteBuffer;

import static com.doge.torrent.utils.ByteUtils.toByte;

public record Message(
	MessageId id,
	byte[] payload
) {
	private static final int MESSAGE_LENGTH_SIZE = 4;
	private static final int MESSAGE_ID_SIZE = 1;
	private static final int REQUEST_PAYLOAD_SIZE = 12;

	public static final Message CHOKE = new Message(MessageId.CHOKE, new byte[0]);
	public static final Message UNCHOKE = new Message(MessageId.UNCHOKE, new byte[0]);
	public static final Message INTERESTED = new Message(MessageId.INTERESTED, new byte[0]);
	public static final Message NOT_INTERESTED = new Message(MessageId.NOT_INTERESTED, new byte[0]);
	public static final Message KEEP_ALIVE = new Message(MessageId.KEEP_ALIVE, new byte[0]);

	public byte[] toBytes() {
		int length = payload.length + MESSAGE_ID_SIZE;
		byte[] bytes = new byte[length + MESSAGE_LENGTH_SIZE];
		ByteBuffer.wrap(bytes).putInt(length);
		bytes[MESSAGE_LENGTH_SIZE] = toByte(id.getId());
		System.arraycopy(payload, 0, bytes, MESSAGE_LENGTH_SIZE + MESSAGE_ID_SIZE, payload.length);

		return bytes;
	}

	public static Message fromBytes(byte[] bytes) {
		int length = ByteBuffer.wrap(bytes).getInt();

		if (length == 0) {
			return new Message(MessageId.KEEP_ALIVE, new byte[0]);
		}

		byte id = bytes[MESSAGE_LENGTH_SIZE];
		byte[] payload = new byte[length - MESSAGE_ID_SIZE];
		System.arraycopy(bytes, MESSAGE_LENGTH_SIZE + MESSAGE_ID_SIZE, payload, 0, payload.length);
		return new Message(MessageId.fromId(id), payload);
	}

	public static Message request(int index, int begin, int length) {
		byte[] payload = new byte[REQUEST_PAYLOAD_SIZE];
		ByteBuffer.wrap(payload).putInt(index).putInt(begin).putInt(length);
		return new Message(MessageId.REQUEST, payload);
	}

	public boolean isKeepAlive() {
		return id == MessageId.KEEP_ALIVE;
	}

	public boolean isPiece() {
		return id == MessageId.PIECE;
	}

}
