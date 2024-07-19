package com.doge.torrent.files.bencode;

import static java.lang.Character.isDigit;

public class StringValidator implements Validator {

	@Override public boolean validate(char c) {
		return isDigit(c);
	}
}
