package com.doge.torrent.files.bencode;

import com.doge.torrent.files.bencode.exception.BencodeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public class Bencode implements TorrentDecoder, TorrentEncoder {

	static final char NUMBER = 'i';

	static final char LIST = 'l';

	static final char DICTIONARY = 'd';

	static final char TERMINATOR = 'e';

	static final char SEPARATOR = ':';

	private final Charset charset;

	public Bencode() {
		this(DEFAULT_CHARSET);
	}

	public Bencode(Charset charset) {
		this.charset = charset;
	}

	@Override
	public BencodeType<?> getType(byte[] bencode) {
		BencodeInputStream is = new BencodeInputStream(new ByteArrayInputStream(bencode));

		try {
			return is.getNextType();
		} catch (IOException e) {
			throw new BencodeException(e);
		}
	}

	@Override
	public <T> T decode(String bencode, BencodeType<T> type) {
		if (bencode == null) {
			throw new BencodeException("Bencode cannot be null");
		}
		if (type == null) {
			throw new BencodeException("Type cannot be null");
		}
		if (type == BencodeType.bencodeUnknown) {
			throw new BencodeException("Type cannot be unknown");
		}

		return decode(bencode.getBytes(charset), type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T decode(byte[] bencode, BencodeType<T> type) {

		if (bencode == null) {
			throw new BencodeException("Bencode cannot be null");
		}
		if (type == null) {
			throw new BencodeException("Type cannot be null");
		}
		if (type == BencodeType.bencodeUnknown) {
			throw new BencodeException("Type cannot be unknown");
		}

		BencodeInputStream is = new BencodeInputStream(new ByteArrayInputStream(bencode));

		try {
			if (type == BencodeType.bencodeString) {
				return (T) is.readString();
			} else if (type == BencodeType.bencodeNumber) {
				return (T) is.readNumber();
			} else if (type == BencodeType.bencodeList) {
				return (T) is.readList();
			} else if (type == BencodeType.bencodeDictionary) {
				return (T) is.readDictionary();
			}
		} catch (IOException e) {
			throw new BencodeException(e);
		}
		throw new BencodeException("Unknown type: " + type);
	}

	@Override
	public byte[] encode(String s) {
		if (s == null) {
			throw new BencodeException("String cannot be null");
		}

		return encode(s, BencodeType.bencodeString);
	}

	@Override
	public byte[] encode(Iterable<?> l) {
		if (l == null) {
			throw new BencodeException("Byte array cannot be null");
		}

		return encode(l, BencodeType.bencodeList);
	}

	@Override
	public byte[] encode(Map<?, ?> m) {
		if (m == null) {
			throw new BencodeException("Map cannot be null");
		}

		return encode(m, BencodeType.bencodeDictionary);
	}

	@Override
	public byte[] encode(Long n) {
		if (n == null) {
			throw new BencodeException("Number cannot be null");
		}

		return encode(n, BencodeType.bencodeNumber);
	}

	private byte[] encode(Object o, BencodeType<?> type) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BencodeOutputStream os = new BencodeOutputStream(output);

		try {
			if (type == BencodeType.bencodeString) {
				os.writeString((String) o);
			} else if (type == BencodeType.bencodeNumber) {
				os.writeLong((Long) o);
			} else if (type == BencodeType.bencodeList) {
				os.writeList((Iterable<?>) o);
			} else if (type == BencodeType.bencodeDictionary) {
				os.writeDictionary((Map<?, ?>) o);
			}
		} catch (IOException e) {
			throw new BencodeException(e);
		}

		return output.toByteArray();
	}

}
