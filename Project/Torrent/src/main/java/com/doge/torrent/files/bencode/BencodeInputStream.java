package com.doge.torrent.files.bencode;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.doge.torrent.files.bencode.BencodeType.bencodeDictionary;
import static com.doge.torrent.files.bencode.BencodeType.bencodeList;
import static com.doge.torrent.files.bencode.BencodeType.bencodeNumber;
import static com.doge.torrent.files.bencode.BencodeType.bencodeString;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class BencodeInputStream extends FilterInputStream {

	private final PushbackInputStream in ;

	protected BencodeInputStream(InputStream in) {
		super(new PushbackInputStream(in));
		this.in = (PushbackInputStream) super.in;
	}

	private Object readObject() throws IOException {
		BencodeType<?> type = getNextType();
		if (type == bencodeString) {
			return readString();
		} else if (type == bencodeNumber) {
			return readNumber();
		} else if (type == bencodeList) {
			return readList();
		} else if (type == bencodeDictionary) {
			return readDictionary();
		}
		throw new IOException("Unknown type: " + type);
	}

	public BencodeType getNextType() throws IOException {
		char token = (char) in.read();
		BencodeType type = BencodeType.fromToken(token);
		in.unread(token);
		return type;
	}

	public Long readNumber() throws IOException {
		char token = (char) read();
		if (!bencodeNumber.validate(token)) {
			throw new IOException("Invalid token: " + token);
		}
		StringBuilder sb = new StringBuilder();
		token = (char) read();
		while (token != Bencode.TERMINATOR) {
			sb.append(token);
			token = (char) read();
		}
		return Long.parseLong(sb.toString());
	}

	public String readString() throws IOException {
		char token = (char) in.read();
		if (!bencodeString.validate(token)) {
			throw new IOException("Invalid token: " + token);
		}

		StringBuilder sb = new StringBuilder();
		while (bencodeString.validate(token)) {
			sb.append(token);
			token = (char) read();
		}
		Long length = Long.parseLong(sb.toString());
		byte[] bytes = new byte[length.intValue()];
		in.read(bytes);
		return new String(bytes, DEFAULT_CHARSET);
	}

	public List<Object> readList() throws IOException {
		char token = (char) in.read();
		if (!bencodeList.validate(token)) {
			throw new IOException("Invalid token: " + token);
		}
		List<Object> list = new ArrayList<>();
		token = (char) in.read();
		in.unread(token);
		while (token != Bencode.TERMINATOR) {
			list.add(readObject());
			token = (char) in.read();
			in.unread(token);
		}
		in.read();
		return list;
	}

	public Map<String, Object> readDictionary() throws IOException {
		char token = (char) in.read();
		if (!bencodeDictionary.validate(token)) {
			throw new IOException("Invalid token: " + token);
		}
		Map<String, Object> map = new HashMap<>();

		while (token != Bencode.TERMINATOR) {
			String key = readString();
			Object value = readObject();
			map.put(key, value);
			token = (char) in.read();
			in.unread(token);
		}
		in.read();
		return map;
	}
}
