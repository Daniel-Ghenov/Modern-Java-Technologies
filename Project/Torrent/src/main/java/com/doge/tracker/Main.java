package com.doge.tracker;

import com.doge.torrent.files.TorrentFileParser;
import com.doge.torrent.files.TorrentFileParserImpl;
import com.doge.torrent.files.bencode.Bencode;
import com.doge.torrent.files.bencode.TorrentEncoder;
import com.doge.tracker.server.TorrentTrackerServer;

public class Main {

	public static void main(String[] args) {
		TorrentFileParser torrentFileParser = new TorrentFileParserImpl();
		TorrentTracker tracker = new TorrentTracker(torrentFileParser);
		tracker.loadTorrents("C:\\Users\\PC-Admin\\Desktop\\Torrent");
		TorrentEncoder encoder = new Bencode();
		TorrentTrackerServer server = new TorrentTrackerServer(tracker, encoder);
		server.start();
	}

}
