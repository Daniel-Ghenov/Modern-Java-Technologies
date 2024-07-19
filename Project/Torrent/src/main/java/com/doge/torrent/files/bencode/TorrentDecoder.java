package com.doge.torrent.files.bencode;

public interface TorrentDecoder {

	BencodeType<?> getType(byte[] bencode);

	<T> T decode(String bencode, BencodeType<T> type);

	<T> T decode(byte[] bencode, BencodeType<T> type);
}
