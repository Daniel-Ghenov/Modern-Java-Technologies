package com.doge.torrent.logging;

public enum Level {
	TRACE(0),
	DEBUG(1),
	INFO(2),
	WARN(3),
	ERROR(4),
	OFF(5);

	private final int level;

	Level(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
