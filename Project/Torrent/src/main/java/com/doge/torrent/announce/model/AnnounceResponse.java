package com.doge.torrent.announce.model;

import java.util.List;

public record AnnounceResponse(
		Long interval,
		List<Peer> peers
) {


}
