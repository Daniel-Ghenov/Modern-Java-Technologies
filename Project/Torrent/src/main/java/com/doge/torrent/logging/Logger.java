package com.doge.torrent.logging;

public interface Logger {

	void debug(String message);

	void info(String message);

	void error(String message);

	void error(String message, Throwable throwable);

	void warn(String message);

	void warn(String message, Throwable throwable);

	void trace(String message);

	void trace(String message, Throwable throwable);

	boolean isDebugEnabled();

	boolean isInfoEnabled();

	boolean isErrorEnabled();

	boolean isWarnEnabled();

	boolean isTraceEnabled();

}
