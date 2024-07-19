package com.doge.torrent.connection.exception;

public class ClientConnectionException extends RuntimeException {

	public ClientConnectionException(String message) {
		super(message);
	}

	public ClientConnectionException(Throwable cause) {
		super(cause);
	}

}
