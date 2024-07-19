package com.doge.torrent.connection.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandshakeTest
{

	private static final String PROTOCOL_IDENTIFIER = "BitTorrent protocol";
	private static final String PROTOCOL_IDENTIFIER_SIZE = "\u0013";
	private static final String RESERVED_BYTES = "\0\0\0\0\0\0\0\0";
	private static final String INFO_HASH = "_INFOHASH_INFOHASH_A";

	private static final String PEER_ID = "PEERIDPEERIDPEERIDID";

	@Test
	void testHandshakeToMessage()
	{
		Handshake handshake = new Handshake(INFO_HASH, PEER_ID);
		byte[] message = handshake.toMessage();
		assertEquals(68, message.length, "Message length should be 68");
		assertEquals(PROTOCOL_IDENTIFIER_SIZE + PROTOCOL_IDENTIFIER + RESERVED_BYTES + INFO_HASH + PEER_ID, new String(message), "Message should be 'protocolIdentifier + reservedBytes + infoHash + peerId'");
	}

	@Test
	void testHandshakeFromMessage()
	{
		byte[] message = (PROTOCOL_IDENTIFIER_SIZE + PROTOCOL_IDENTIFIER + RESERVED_BYTES + INFO_HASH + PEER_ID).getBytes();
		Handshake handshake = Handshake.fromMessage(message);
		assertEquals(INFO_HASH, handshake.infoHash(), "Info hash should be 'infoHash'");
		assertEquals(PEER_ID, handshake.peerId(), "Peer id should be 'peerId'");
	}

	@Test
	void testValidateClientHandshakeWhenInfoHashDoesNotMatchShouldThrowHandshakeException()
	{
		Handshake handshake = new Handshake(INFO_HASH, PEER_ID);
		Handshake clientHandshake = new Handshake("otherInfoHash------", PEER_ID);
		assertThrows(HandshakeException.class, () -> handshake.validatePeerHandshake(clientHandshake), "Should throw HandshakeException");
	}

	@Test
	void testValidateClientHandshakeWhenInfoHashMatchesShouldNotThrowHandshakeException()
	{
		Handshake handshake = new Handshake(INFO_HASH, PEER_ID);
		Handshake clientHandshake = new Handshake(INFO_HASH, PEER_ID);
		assertDoesNotThrow(() -> handshake.validatePeerHandshake(clientHandshake), "Should not throw HandshakeException");
	}

}