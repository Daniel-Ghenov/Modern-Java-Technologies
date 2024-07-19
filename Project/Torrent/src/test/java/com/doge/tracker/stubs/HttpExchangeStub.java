package com.doge.tracker.stubs;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class HttpExchangeStub extends HttpExchange {

	private String requestMethod;

	private URI requestURI;

	private InetSocketAddress remoteAddress;

	private OutputStream responseBody;

	private int responseCode;

	private long responseLength;

	public HttpExchangeStub(String requestMethod, URI requestURI, InetSocketAddress remoteAddress, OutputStream responseBody) {
		this.requestMethod = requestMethod;
		this.requestURI = requestURI;
		this.remoteAddress = remoteAddress;
		this.responseBody = responseBody;
	}

	@Override public Headers getRequestHeaders() {
		return null;
	}

	@Override public Headers getResponseHeaders() {
		return null;
	}

	@Override public URI getRequestURI() {
		return requestURI;
	}

	@Override public String getRequestMethod() {
		return requestMethod;
	}

	@Override public HttpContext getHttpContext() {
		return null;
	}

	@Override public void close() {

	}

	@Override public InputStream getRequestBody() {
		return null;
	}

	@Override public OutputStream getResponseBody() {
		return responseBody;
	}

	@Override public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
		this.responseCode = rCode;
		this.responseLength = responseLength;
	}

	@Override public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override public int getResponseCode() {
		return responseCode;
	}

	@Override public InetSocketAddress getLocalAddress() {
		return null;
	}

	@Override public String getProtocol() {
		return null;
	}

	@Override public Object getAttribute(String name) {
		return null;
	}

	@Override public void setAttribute(String name, Object value) {

	}

	@Override public void setStreams(InputStream i, OutputStream o) {

	}

	@Override public HttpPrincipal getPrincipal() {
		return null;
	}
}
