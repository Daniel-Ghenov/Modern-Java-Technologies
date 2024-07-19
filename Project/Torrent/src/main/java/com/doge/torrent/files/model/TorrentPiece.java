package com.doge.torrent.files.model;

public record TorrentPiece(
		byte[] hash,
		int index,
		long pieceLength
) {

}
