package com.doge.torrent.announce.exception;

public class AnnouncementException extends RuntimeException {
	private AnnouncementError error;

	public AnnouncementException(String message) {
		super(message);
		this.error = AnnouncementError.parseError(message);
	}

	public AnnouncementException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnouncementError getError() {
		return error;
	}

}
