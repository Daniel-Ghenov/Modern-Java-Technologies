package com.doge.torrent.download.files;

import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileTorrentSaver implements TorrentSaver {

	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(FileTorrentSaver.class);

	private final String path;

	public FileTorrentSaver(String path) {
		this.path = path;
	}

	@Override
	public void save(byte[] data) {
		try (FileOutputStream fileOutputStream = new FileOutputStream(path)) {
			fileOutputStream.write(data);
		} catch (IOException e) {
			LOGGER.error("Failed to save file");
		}

	}
}
