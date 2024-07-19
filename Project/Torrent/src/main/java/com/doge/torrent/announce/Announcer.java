package com.doge.torrent.announce;

import com.doge.torrent.announce.model.AnnounceRequest;
import com.doge.torrent.announce.model.AnnounceResponse;

public interface Announcer {

	AnnounceResponse announce(AnnounceRequest request);

}
