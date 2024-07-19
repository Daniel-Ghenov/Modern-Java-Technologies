package com.doge.torrent.files.bencode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BencodeType<T> {
	public static BencodeType<String> bencodeString = new BencodeType<>(new StringValidator());
	public static BencodeType<Long> bencodeNumber = new BencodeType<>(new CharValidator(Bencode.NUMBER));
	public static BencodeType<List<Object>> bencodeList = new BencodeType<>(new CharValidator(Bencode.LIST));
	public static BencodeType<Map<String, Object>> bencodeDictionary =
			new BencodeType<>(new CharValidator(Bencode.DICTIONARY));
	public static BencodeType<Void> bencodeUnknown = new BencodeType<>(new Validator() {
		@Override public boolean validate(char c) {
			return false;
		}
	});

	private final Validator validator;

	private BencodeType(Validator validator) {
		this.validator = validator;
	}

	public boolean validate(char c) {
		return validator.validate(c);
	}

	public static BencodeType fromToken(char token) {
		return Arrays.stream(BencodeType.values())
			  .filter(type -> type.validate(token))
			  .findFirst()
			  .orElse(bencodeUnknown);
	}

	public static BencodeType[] values() {
		return new BencodeType[]{bencodeString, bencodeNumber, bencodeList, bencodeDictionary, bencodeUnknown};
	}
}
