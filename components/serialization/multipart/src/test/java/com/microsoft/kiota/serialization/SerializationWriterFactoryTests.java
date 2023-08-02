package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SerializationWriterFactoryTests {
	private static final MultipartSerializationWriterFactory _serializationWriterFactory = new MultipartSerializationWriterFactory();
	private static final String contentType = "multipart/form-data";
	@Test
	public void getsWriterForMultipartContentType() {
		final var serializationWriter = _serializationWriterFactory.getSerializationWriter(contentType);
		assertNotNull(serializationWriter);
	}
	@Test
	public void throwsArgumentOutOfRangeExceptionForInvalidContentType() {
		assertThrows(IllegalArgumentException.class, () -> _serializationWriterFactory.getSerializationWriter("application/json"));
	}
	@Test
	public void throwsArgumentNullExceptionForNoContentType() {
		assertThrows(NullPointerException.class, () -> _serializationWriterFactory.getSerializationWriter(""));
	}
}
