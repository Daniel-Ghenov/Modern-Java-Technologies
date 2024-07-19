package com.doge.torrent.announce.exception;

import java.util.Arrays;

public enum AnnouncementError {

	PEER_NOT_FOUND("Peer not found"),
	INVALID_INFO_HASH("Invalid info hash"),
	INVALID_PEER_ID("Invalid peer id"),
	MISSING_INFO_HASH("Missing info hash"),
	UNKNOWN_ERROR("Unknown error"),
	MISSING_PORT("Missing port");

	private final String message;

	AnnouncementError(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public static AnnouncementError parseError(String message) {
		return Arrays.stream(values())
				.filter(error -> error.getMessage().contains(message))
				.findFirst()
				.orElse(UNKNOWN_ERROR);
	}

}
