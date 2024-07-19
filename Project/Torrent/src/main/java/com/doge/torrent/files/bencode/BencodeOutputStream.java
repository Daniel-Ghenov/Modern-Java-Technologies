package com.doge.torrent.files.bencode;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class BencodeOutputStream extends FilterOutputStream {

	public BencodeOutputStream(OutputStream out) {
		super(out);
	}

	private byte[] encode(String s) throws IOException {
		return encode(s.getBytes(DEFAULT_CHARSET));
	}

	public void writeString(String s) throws IOException {
		write(encode(s));
	}

	public void writeString(byte[] bytes) throws IOException {
		write(encode(bytes));
	}

	public void writeLong(Long n) throws IOException {
		write(encode(n));
	}

	public void writeList(Iterable<?> l) throws IOException {
		write(encode(l));
	}

	public void writeDictionary(Map<?, ?> m) throws IOException {
		write(encode(m));
	}

	private byte[] encode(byte[] bytes) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		os.write(Integer.toString(bytes.length).getBytes());
		os.write(Bencode.SEPARATOR);
		os.write(bytes);

		return os.toByteArray();
	}

	private byte[] encode(Long n) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(Bencode.NUMBER);
		os.write(Long.toString(n).getBytes());
		os.write(Bencode.TERMINATOR);

		return os.toByteArray();
	}

	private byte[] encode(Iterable<?> l) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(Bencode.LIST);
		for (Object o : l) {
			os.write(encodeObject(o));
		}
		os.write(Bencode.TERMINATOR);

		return os.toByteArray();
	}

	private byte[] encode(Map<?, ?> m) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(Bencode.DICTIONARY);

		if (!(m instanceof SortedMap<?, ?>)) {
			m = new TreeMap<>(m);
		}

		for (Map.Entry<?, ?> entry : m.entrySet()) {
			try {
				os.write(encode(entry.getKey().toString()));
				os.write(encodeObject(entry.getValue()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		os.write(Bencode.TERMINATOR);

		return os.toByteArray();
	}

	private byte[] encodeObject(Object value) throws IOException {
		if (value == null) {
			throw new IOException("Value cannot be null");
		}

		return switch (value) {
			case String s -> encode(s);
			case byte[] bytes -> encode(bytes);
			case Long l -> encode(l);
			case Iterable<?> iterable -> encode(iterable);
			case Map<?, ?> map -> encode(map);
			default -> encode(value.toString());
		};

	}
}

