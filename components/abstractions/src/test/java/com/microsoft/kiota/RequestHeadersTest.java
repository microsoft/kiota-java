package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class RequestHeadersTest {
	@Test
	void Adds() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		assertTrue(requestHeaders.isEmpty());
		// Act
		requestHeaders.add("key", "value");
		// Assert
		assertEquals(1, requestHeaders.size());
		assertEquals(1, requestHeaders.get("key").size());
		assertEquals("value", requestHeaders.get("key").iterator().next());
		assertTrue(requestHeaders.containsKey("key"));
		assertFalse(requestHeaders.isEmpty());
	}
	@Test
	void NormalizesKey() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		// Act
		requestHeaders.add("Key", "value");
		// Assert
		assertEquals(1, requestHeaders.size());
		assertEquals(1, requestHeaders.get("key").size());
		assertEquals("value", requestHeaders.get("key").iterator().next());
	}
	@Test
	void Puts() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		assertTrue(requestHeaders.isEmpty());
		// Act
		requestHeaders.put("key", new HashSet<String>() {{
			add("value");
		}});
		// Assert
		assertEquals(1, requestHeaders.size());
		assertEquals(1, requestHeaders.get("key").size());
		assertEquals("value", requestHeaders.get("key").iterator().next());
		assertTrue(requestHeaders.containsKey("key"));
		assertFalse(requestHeaders.isEmpty());
	}
	@Test
	void Removes() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		requestHeaders.add("key", "value");
		assertEquals(1, requestHeaders.size());
		// Act
		requestHeaders.remove("key");
		// Assert
		assertEquals(0, requestHeaders.size());
		assertTrue(requestHeaders.isEmpty());
	}
	@Test
	void PutsAll() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		assertTrue(requestHeaders.isEmpty());
		// Act
		requestHeaders.putAll(new RequestHeaders() {{
			add("key", "value");
		}});
		// Assert
		assertEquals(1, requestHeaders.size());
		assertEquals(1, requestHeaders.get("key").size());
		assertEquals("value", requestHeaders.get("key").iterator().next());
		assertTrue(requestHeaders.containsKey("key"));
		assertFalse(requestHeaders.isEmpty());
	}
	@Test
	void Clears() {
		// Arrange
		final RequestHeaders requestHeaders = new RequestHeaders();
		requestHeaders.add("key", "value");
		assertEquals(1, requestHeaders.size());
		// Act
		requestHeaders.clear();
		// Assert
		assertEquals(0, requestHeaders.size());
		assertTrue(requestHeaders.isEmpty());
	}
}
