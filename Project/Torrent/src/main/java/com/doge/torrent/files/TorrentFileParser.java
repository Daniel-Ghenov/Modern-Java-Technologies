package com.doge.torrent.files;

import com.doge.torrent.files.model.TorrentFile;

import java.util.List;

public interface TorrentFileParser {

	TorrentFile parse(byte[] content);

	TorrentFile parseFromPath(String path);

	List<TorrentFile> parseAllFromPath(String path);
}
