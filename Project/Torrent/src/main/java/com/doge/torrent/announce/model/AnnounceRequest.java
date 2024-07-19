package com.doge.torrent.announce.model;

public record AnnounceRequest(
		String trackerAnnounceUrl,
		String infoHash,
		String peerId,
		Long downloaded,
		Long uploaded,
		Long left,
		Integer port,
		Boolean compact,
		Event event
) {

}
