package com.doge.torrent.files.bencode;

import java.util.Map;

public interface TorrentEncoder {

	byte[] encode(String s);

	byte[] encode(Iterable<?> l);

	byte[] encode(Map<?, ?> m);

	byte[] encode(Long n);
}
