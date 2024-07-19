package com.doge.torrent.connection.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

	@ParameterizedTest(name = "testMessageToBytesWhenMessage{0}")
	@EnumSource(MessageId.class)
	void testMessageToBytesWhenNoPayload(MessageId messageId) {
		Message message = new Message(messageId, new byte[0]);
		byte[] bytes = message.toBytes();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		assertEquals(5, bytes.length, "Message length should be 5");
		assertEquals(1, buffer.getInt(), "Message length should be 1");
		assertEquals(messageId.getId(), buffer.get(), "Message id should be 0");
	}

	@Test
	void testMessageToBytesWhenHasPayload() {
		Message message = new Message(MessageId.CHOKE, new byte[]{1, 2, 3, 4});
		byte[] bytes = message.toBytes();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		assertEquals(9, bytes.length, "Message length should be 9");
		assertEquals(5, buffer.getInt(), "Message length should be 5");
		assertEquals(MessageId.CHOKE.getId(), buffer.get(), "Message id should be 0");
		assertArrayEquals(new byte[]{1, 2, 3, 4}, message.payload(), "Payload should be 'payload'");
	}

	@Test
	void testMessageFromBytesWhenKeepAlive() {
		byte[] bytes = new byte[]{0, 0, 0, 0};
		Message message = Message.fromBytes(bytes);
		assertEquals(MessageId.KEEP_ALIVE, message.id(), "Message id should be 'KEEP_ALIVE'");
		assertArrayEquals(new byte[0], message.payload(), "Payload should be empty");
	}

	@Test
	void testMessageFromBytesWhenNotKeepAlive() {
		byte[] bytes = new byte[]{0, 0, 0, 5, 0, 1, 2, 3, 4};
		Message message = Message.fromBytes(bytes);
		assertEquals(MessageId.CHOKE, message.id(), "Message id should be 'CHOKE'");
		assertArrayEquals(new byte[]{1, 2, 3, 4}, message.payload(), "Payload should be 'payload'");
	}

	@Test
	void testIsKeepAliveWhenKeepAliveShouldReturnTrue() {
		Message message = new Message(MessageId.KEEP_ALIVE, new byte[0]);
		assertTrue(message.isKeepAlive(), "Should return true");
	}

	@Test
	void testIsKeepAliveWhenNotKeepAliveShouldReturnFalse() {
		Message message = new Message(MessageId.CHOKE, new byte[0]);
		assertFalse(message.isKeepAlive(), "Should return false");
	}

}