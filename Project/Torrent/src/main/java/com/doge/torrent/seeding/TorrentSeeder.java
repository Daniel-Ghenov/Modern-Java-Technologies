package com.doge.torrent.seeding;

import com.doge.torrent.connection.message.BitField;
import com.doge.torrent.connection.piece.PieceProgress;

import java.nio.ByteBuffer;
import java.util.Map;

public class TorrentSeeder {
	private final Map<String, Map<Integer, PieceProgress>> piecesByIndexByInfoHash;
	private final Map<String, Integer> pieceCountByInfoHash;

	public TorrentSeeder(Map<String, Map<Integer, PieceProgress>> piecesByIndexByInfoHash,
						 Map<String, Integer> pieceCountByInfoHash) {
		this.piecesByIndexByInfoHash = piecesByIndexByInfoHash;
		this.pieceCountByInfoHash = pieceCountByInfoHash;
	}

	public BitField getBitField(String infoHash) {
		Integer pieceCount = pieceCountByInfoHash.get(infoHash);
		var bitField = new BitField(pieceCount);
		piecesByIndexByInfoHash.get(infoHash).forEach((index, piece) -> {
			if (piece.isComplete()) {
				bitField.setPiece(index);
			}
		});
		return bitField;
	}

	public boolean hasInfoHash(String infoHash) {
		return piecesByIndexByInfoHash.containsKey(infoHash);
	}

	public byte[] getPiece(String infoHash, int index, int begin, int length) {
		PieceProgress piece = piecesByIndexByInfoHash.get(infoHash).get(index);
		if (piece == null || piece.pieceLength() < begin + length) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(piece.data()).position(begin);
		byte[] block = new byte[length];
		buffer.get(block);
		return block;
	}
}
