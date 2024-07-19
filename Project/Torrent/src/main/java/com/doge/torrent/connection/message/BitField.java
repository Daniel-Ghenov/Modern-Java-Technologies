package com.doge.torrent.connection.message;

public record BitField(
		byte[] bitField
) {

	public static final int BYTE_LENGTH = 8;

	public BitField(int pieceCount) {
		this(new byte[(int) Math.ceil(pieceCount / (double) BYTE_LENGTH)]);
	}

	public boolean hasPiece(int piece) {
		if (piece >= bitField.length * BYTE_LENGTH
				|| piece < 0) {
			return false;
		}

		int bytePos = piece / BYTE_LENGTH;
		int bitPos = piece % BYTE_LENGTH;
		return (bitField[bytePos] & (1 << BYTE_LENGTH - bitPos - 1)) > 0;
	}

	public void setPiece(int piece) {
		int bytePos = piece / BYTE_LENGTH;
		int bitPos = piece % BYTE_LENGTH;
		bitField[bytePos] |= (byte) (1 << BYTE_LENGTH - bitPos - 1);
	}

	public static BitField fromMessage(Message message) {
		if (message.id() != MessageId.BITFIELD) {
			throw new IllegalArgumentException("Message is not a bitfield");
		}

		return new BitField(message.payload());
	}

	public byte[] toBytes() {
		return new Message(MessageId.BITFIELD, bitField).toBytes();
	}

	public boolean isEmpty() {
		for (byte b : bitField) {
			if (b != 0) {
				return false;
			}
		}
		return true;
	}
}
