package com.doge.torrent.logging;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TorrentLoggerFactory {

	private static String staticLoggersName = null;
	private static Level level = Level.TRACE;

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

	public static Logger getLogger(Class<?> clazz) {
		String name = Optional.ofNullable(staticLoggersName).orElse(clazz.getSimpleName());

		return new ConsoleLogger(level, name);
	}

	public static void setStaticLoggersName(String staticLoggersName) {
		TorrentLoggerFactory.staticLoggersName = staticLoggersName;
	}

	public static void setLevel(Level level) {
		TorrentLoggerFactory.level = level;
	}

	public static void setFormatter(DateTimeFormatter formatter) {
		TorrentLoggerFactory.formatter = formatter;
	}

}
