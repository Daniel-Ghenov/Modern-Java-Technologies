package com.doge.torrent.files.model;

import java.util.List;
import java.util.Map;

public record SourceFile(
		List<String> path,
		Long length
) {

	@SuppressWarnings("unchecked")
	public static SourceFile fromMap(Map<String, Object> map) {
		try {
			List<String> path = (List<String>) map.get("path");
			Long length = (Long) map.get("length");
			return new SourceFile(path, length);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Invalid map: " + map);
		}
	}

}
