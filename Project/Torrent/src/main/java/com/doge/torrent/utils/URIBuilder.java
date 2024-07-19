package com.doge.torrent.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class URIBuilder {

	private final StringBuilder builder;

	private URIBuilder(String url) {
		this.builder = new StringBuilder(url);
	}

	public static URIBuilder fromURL(String url) {
		return new URIBuilder(url);
	}

	public URIBuilder queryParam(String key, Iterable<?> values) {
		for (Object value : values) {
			queryParam(key, value);
		}
		return this;
	}

	public URIBuilder queryParam(String key, Object value, Charset charset) {
		if (value == null) {
			return this;
		}

		if (builder.indexOf("?") == -1) {
			builder.append("?");
		} else {
			builder.append("&");
		}
		String encodedKey = URLEncoder.encode(key, charset);
		String encodedValue = URLEncoder.encode(value.toString(), charset);
		builder.append(encodedKey).append("=").append(encodedValue);
		return this;
	}

	public URIBuilder queryParam(String key, Object value) {
		return queryParam(key, value, StandardCharsets.UTF_8);
	}

	public String build() {
		return builder.toString();
	}

	public URI buildURI() {
		return URI.create(build());
	}

}
