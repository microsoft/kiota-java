package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class RequestHeadersTest {
	@Test
	public void Adds() {
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
	public void NormalizesKey() {
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
	public void Puts() {
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
	public void Removes() {
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
	public void PutsAll() {
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
}
