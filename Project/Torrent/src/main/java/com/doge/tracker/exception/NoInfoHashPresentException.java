package com.doge.tracker.exception;

public class NoInfoHashPresentException extends RuntimeException {
	public NoInfoHashPresentException(String message) {
		super(message);
	}

	public NoInfoHashPresentException(String message, Throwable cause) {
		super(message, cause);
	}
}
