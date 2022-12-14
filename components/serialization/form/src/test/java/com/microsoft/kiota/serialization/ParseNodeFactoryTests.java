package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

public class ParseNodeFactoryTests {
	private final FormParseNodeFactory _parseNodeFactory = new FormParseNodeFactory();
	private final String contentType = "application/x-www-form-urlencoded";
	@Test
	public void getsWriterForFormContentType() {
		final var initialString = "key1=value1&key2=value2";
		final var rawResponse = new ByteArrayInputStream(initialString.getBytes());
		final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
		assertNotNull(parseNode);
	}
	@Test
	public void throwsArgumentOutOfRangeExceptionForInvalidContentType() {
		final var initialString = "key1=value1&key2=value2";
		final var rawResponse = new ByteArrayInputStream(initialString.getBytes());
		assertThrows(IllegalArgumentException.class, () -> _parseNodeFactory.getParseNode("application/json", rawResponse));
	}
	@Test
	public void throwsArgumentNullExceptionForNoContentType() {
		final var initialString = "key1=value1&key2=value2";
		final var rawResponse = new ByteArrayInputStream(initialString.getBytes());
		assertThrows(NullPointerException.class, () -> _parseNodeFactory.getParseNode("", rawResponse));
	}
}
