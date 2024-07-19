package com.doge.tracker.model;

import com.doge.torrent.announce.model.Peer;

import java.util.List;

public record TorrentTrackerResponse(
		Long interval,
		List<Peer> peers
) {

}
