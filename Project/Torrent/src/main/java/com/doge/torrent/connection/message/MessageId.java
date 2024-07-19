package com.doge.torrent.connection.message;

import java.util.Arrays;

public enum MessageId {
	CHOKE(0),
	UNCHOKE(1),
	INTERESTED(2),
	NOT_INTERESTED(3),
	HAVE(4),
	BITFIELD(5),
	REQUEST(6),
	PIECE(7),
	CANCEL(8),
	LISTEN_PORT(9),
	KEEP_ALIVE(-1);

	private final int id;

	MessageId(int id) {
		this.id = id;
	}

	public static MessageId fromId(byte id) {
		return Arrays.stream(MessageId.values())
				.filter(messageId -> messageId.id == id)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No message id found for id: " + id));
	}

	public int getId() {
		return id;
	}
}
