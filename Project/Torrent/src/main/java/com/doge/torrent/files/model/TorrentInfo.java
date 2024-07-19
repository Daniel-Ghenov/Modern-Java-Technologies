package com.doge.torrent.files.model;

import java.util.List;

public record TorrentInfo(
		Long length,
		String name,
		Long pieceLength,
		List<SourceFile> files,
		List<TorrentPiece> pieces
) {

	public TorrentInfo {
		if (length == null) {
			length = files.stream().mapToLong(SourceFile::length).sum();
		}
	}

	public static final int PIECE_BYTE_LENGTH = 20;

}
