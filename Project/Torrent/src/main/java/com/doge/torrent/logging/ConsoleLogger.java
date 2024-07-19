package com.doge.torrent.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ConsoleLogger implements Logger {

	private static Level level = Level.TRACE;

	private final String name;

	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

	private static String messageFormat = "%s [%-15s] %-5s : %s";

	public ConsoleLogger(String name) {
		this(Level.INFO, name);
	}

	public ConsoleLogger(Level level, String name) {
		ConsoleLogger.level = level;
		this.name = name;
	}

	public static void setFormatter(DateTimeFormatter formatter) {
		ConsoleLogger.timeFormatter = formatter;
	}

	public static void setMessageFormat(String messageFormat) {
		ConsoleLogger.messageFormat = messageFormat;
	}

	@Override
	public void debug(String message) {
		if (!isDebugEnabled()) {
			return;
		}
		log(message, Level.DEBUG);
	}

	@Override
	public void info(String message) {
		if (!isInfoEnabled()) {
			return;
		}
		log(message, Level.INFO);
	}

	@Override
	public void error(String message) {
		if (!isErrorEnabled()) {
			return;
		}
		log(message, Level.ERROR);
	}

	@Override
	public void error(String message, Throwable throwable) {
		if (!isErrorEnabled()) {
			return;
		}
		List<String> trace = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList();
		log(message + String.join("\n", trace), Level.ERROR);
	}

	@Override
	public void warn(String message) {
		if (!isWarnEnabled()) {
			return;
		}
		log(message, Level.WARN);
	}

	@Override
	public void warn(String message, Throwable throwable) {
		if (!isWarnEnabled()) {
			return;
		}
		List<String> trace = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList();
		log(message + String.join("\n", trace), Level.WARN);
	}

	@Override
	public void trace(String message) {
		if (!isTraceEnabled()) {
			return;
		}
		log(message, Level.TRACE);
	}

	@Override
	public void trace(String message, Throwable throwable) {
		if (!isTraceEnabled()) {
			return;
		}
		List<String> trace = Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList();
		log(message + String.join("\n\t", trace), Level.TRACE);
	}

	private void log(String message, Level level) {
		LocalDateTime now = LocalDateTime.now();
		String newMessage = String.format(messageFormat,
										  now.format(timeFormatter),
										  name,
										  level.name(),
										  message);
		System.out.println(newMessage);
	}

	@Override
	public boolean isDebugEnabled() {
		return level.getLevel() <= Level.DEBUG.getLevel();
	}

	@Override
	public boolean isInfoEnabled() {
		return level.getLevel() <= Level.INFO.getLevel();
	}

	@Override
	public boolean isErrorEnabled() {
		return level.getLevel() <= Level.ERROR.getLevel();
	}

	@Override
	public boolean isWarnEnabled() {
		return level.getLevel() <= Level.WARN.getLevel();
	}

	@Override
	public boolean isTraceEnabled() {
		return level.getLevel() <= Level.TRACE.getLevel();
	}
}
