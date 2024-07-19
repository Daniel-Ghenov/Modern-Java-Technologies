package com.doge.torrent.files.bencode;

public class CharValidator implements Validator {
	private final char c;

	public CharValidator(char c) {
		this.c = c;
	}

	@Override public boolean validate(char c) {
		return c == this.c;
	}
}
