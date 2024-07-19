package com.doge.torrent.connection.piece;

import com.doge.torrent.connection.message.Message;
import com.doge.torrent.files.model.TorrentPiece;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class PieceProgress {

	private static final int PIECE_BEGIN_IDX = 8;

	private final int pieceIndex;
	private final long pieceLength;
	private final byte[] hash;
	private int downloaded;
	private int requested;
	private final boolean verified;
	private byte[] data;

	public PieceProgress(
			int pieceIndex,
			long pieceLength,
			byte[] hash,
			int downloaded,
			int requested,
			boolean verified) {
		this.pieceIndex = pieceIndex;
		this.pieceLength = pieceLength;
		this.hash = hash;
		this.downloaded = downloaded;
		this.requested = requested;
		this.verified = verified;
		data = new byte[(int) pieceLength];
	}

	public PieceProgress(TorrentPiece piece) {
		this(piece.index(), piece.pieceLength(), piece.hash(), 0, 0, false);
	}

	public boolean isComplete() {
		return downloaded >= pieceLength;
	}

	public void addBlock(Message response) {
		byte[] payload = response.payload();
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		int index = buffer.getInt();
		int begin = buffer.getInt();
		int length = payload.length - PIECE_BEGIN_IDX;
		if (index != pieceIndex || begin != requested) {
			throw new IllegalArgumentException("Invalid block");
		}
		downloaded += length;
		requested += length;

		data = ByteBuffer.wrap(data).position(begin).put(payload, PIECE_BEGIN_IDX, length).array();
	}

	public int pieceIndex() {
		return pieceIndex;
	}

	public long pieceLength() {
		return pieceLength;
	}

	public byte[] hash() {
		return hash;
	}

	public int downloaded() {
		return downloaded;
	}

	public int requested() {
		return requested;
	}

	public boolean verified() {
		return verified;
	}

	public byte[] data() {
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (PieceProgress) obj;
		return this.pieceIndex == that.pieceIndex &&
			   this.pieceLength == that.pieceLength &&
			   Objects.equals(this.hash, that.hash) &&
			   this.downloaded == that.downloaded &&
			   this.requested == that.requested &&
			   this.verified == that.verified;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pieceIndex, pieceLength, hash, downloaded, requested, verified);
	}

	@Override
	public String toString() {
		return "PieceProgress[" +
			   "pieceIndex=" + pieceIndex + ", " +
			   "pieceLength=" + pieceLength + ", " +
			   "hash=" + hash + ", " +
			   "downloaded=" + downloaded + ", " +
			   "requested=" + requested + ", " +
			   "verified=" + verified + ']';
	}
}
