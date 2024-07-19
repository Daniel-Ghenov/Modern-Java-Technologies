package com.doge.torrent.announce.model;

import java.net.InetSocketAddress;

import static com.doge.torrent.utils.ByteUtils.toUnsignedByte;
import static com.doge.torrent.utils.Constants.DEFAULT_CHARSET;

public record Peer(
		InetSocketAddress address,
		String peerId
) {

	public static final int PEER_BYTE_LENGTH_NO_ID = 6;
	public static final int PEER_BYTE_LENGTH_WITH_ID = 26;
	private static final int IP_LENGTH = 4;
	private static final int PORT_START = 4;
	private static final int PORT_END = 5;
	private static final int PORT_START_SHIFT = 8;

	public Peer(String ip, Integer port, String peerId) {
		this(new InetSocketAddress(ip, port), peerId);
	}

	public static Peer fromByteArr(byte[] bytes) {
		String ip = getIp(bytes);
		Integer port = getPort(bytes);
		String peerId = getPeerId(bytes);
		return new Peer(new InetSocketAddress(ip, port), peerId);
	}

	private static String getPeerId(byte[] bytes) {
		if (bytes.length < PEER_BYTE_LENGTH_WITH_ID) {
			return null;
		}
		return new String(bytes, PEER_BYTE_LENGTH_NO_ID,
						  PEER_BYTE_LENGTH_WITH_ID - PEER_BYTE_LENGTH_NO_ID,
						  DEFAULT_CHARSET);
	}

	private static String getIp(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < IP_LENGTH; i++) {
			sb.append(toUnsignedByte(bytes[i]));
			if (i != IP_LENGTH - 1) {
				sb.append(".");
			}
		}
		//TODO: remove this check
		String ip =  sb.toString();
		if (ip.equals("92.247.249.116")) {
			return "127.0.0.1";
		}
		return ip;
	}

	private static Integer getPort(byte[] bytes) {
		return (toUnsignedByte(bytes[PORT_START]) << PORT_START_SHIFT) | toUnsignedByte(bytes[PORT_END]);
	}
}
