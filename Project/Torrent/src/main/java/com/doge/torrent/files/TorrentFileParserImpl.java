package com.doge.torrent.files;

import com.doge.torrent.files.bencode.Bencode;
import com.doge.torrent.files.bencode.TorrentDecoder;
import com.doge.torrent.files.model.SourceFile;
import com.doge.torrent.files.model.TorrentFile;
import com.doge.torrent.files.bencode.BencodeType;
import com.doge.torrent.files.model.TorrentInfo;
import com.doge.torrent.files.model.TorrentPiece;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.doge.torrent.files.hasher.TorrentHasher.hashEncodedMap;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class TorrentFileParserImpl implements TorrentFileParser {

	private final TorrentDecoder decoder;

	public TorrentFileParserImpl() {
		this(new Bencode());
	}

	public TorrentFileParserImpl(TorrentDecoder decoder) {
		this.decoder = decoder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TorrentFile parse(byte[] content) {
		Map<String, Object> dict = decoder.decode(content, BencodeType.bencodeDictionary);
		String announce = (String) dict.get("announce");
		List<String> announceList = parseAnnounceList(dict);
		List<String> urlList = (List<String>) dict.get("url-list");
		Map<String, Object> infoMap = (Map<String, Object>) dict.get("info");
		TorrentInfo info = fromMap(infoMap);
		String infoHash = hashEncodedMap(infoMap);
		return new TorrentFile(announce, announceList, urlList, info, infoHash);
	}

	@SuppressWarnings("unchecked")
	public static TorrentInfo fromMap(Map<String, Object> map) {
		Long length = (Long) map.get("length");
		String name = (String) map.get("name");
		Long pieceLength = (Long) map.get("piece length");
		List<TorrentPiece> pieces = getPieces((String) map.get("pieces"), pieceLength, length);
		List<Map<String, Object>> filesObjects = (List<Map<String, Object>>) map.get("files");
		List<SourceFile> files = null;

		if (filesObjects == null) {
			files = new ArrayList<>();
		} else {
			files = filesObjects.stream().map(SourceFile::fromMap).toList();
		}
		return new TorrentInfo(length, name, pieceLength, files, pieces);
	}

	private static List<TorrentPiece> getPieces(String pieces, Long pieceLength, Long totalLength) {
		List<TorrentPiece> chunks = new ArrayList<>();
		for (int i = 0; i < pieces.length(); i += TorrentInfo.PIECE_BYTE_LENGTH) {
			String hash = pieces.substring(i, Math.min(pieces.length(), i + TorrentInfo.PIECE_BYTE_LENGTH));
			TorrentPiece piece = new TorrentPiece(hash.getBytes(DEFAULT_CHARSET),
									  i / TorrentInfo.PIECE_BYTE_LENGTH,
									  pieceLength);
			StringBuilder hexString = new StringBuilder();
			for (byte b : piece.hash()) {
				hexString.append(String.format("%02x", b));
			}
			chunks.add(piece);
		}
		if (totalLength % pieceLength != 0) {
			TorrentPiece lastPiece = chunks.getLast();
			chunks.remove(chunks.size() - 1);
			chunks.add(new TorrentPiece(lastPiece.hash(), lastPiece.index(), totalLength % pieceLength));
		}
		return chunks;
	}

	@SuppressWarnings("unchecked")
	private static List<String> parseAnnounceList(Map<String, Object> map) {
		List<List<String>> announceList = (List<List<String>>) map.get("announce-list");
		if (announceList == null) {
			return List.of();
		}
		return announceList.stream().flatMap(List::stream).toList();
	}

	@Override
	public TorrentFile parseFromPath(String path) {
		try {
			byte[] content = Files.readAllBytes(Paths.get(path));
			return parse(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public List<TorrentFile> parseAllFromPath(String path) {
		Path dir = Paths.get(path);
		List<TorrentFile> files = new ArrayList<>();

		try (var stream = Files.walk(dir)) {
			stream.filter(Files::isRegularFile)
				 .forEach(file -> tryToAdd(file, files));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return files;
	}

	private boolean tryToAdd(Path file, List<TorrentFile> files) {
		try {
			files.add(parseFromPath(file.toString()));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
