package com.microsoft.kiota.serialization;
/**
 * Serialization helpers for kiota models.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import jakarta.annotation.Nonnull;

public final class KiotaSerialization {
	private static final String CHARSET_NAME = "UTF-8";
	private KiotaSerialization() {}
	/**
	 * Serializes the given value to a stream
	 * @param <T> the type of the value to serialize
	 * @param contentType the content type to use for serialization
	 * @param value the value to serialize
	 * @return the serialized value as a stream
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> InputStream serializeAsStream(@Nonnull final String contentType, @Nonnull final T value) throws IOException {
		try(final SerializationWriter writer = getSerializationWriter(contentType, value)) {
			writer.writeObjectValue("", value);
			return writer.getSerializedContent();
		}
	}
	/**
	 * Serializes the given value to a string
	 * @param <T> the type of the value to serialize
	 * @param contentType the content type to use for serialization
	 * @param value the value to serialize
	 * @return the serialized value as a string
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> String serializeAsString(@Nonnull final String contentType, @Nonnull final T value) throws IOException {
		try(final InputStream stream = serializeAsStream(contentType, value)) {
			return new String(stream.readAllBytes(), CHARSET_NAME);
		}
	}
	/**
	 * Serializes the given value to a stream
	 * @param <T> the type of the value to serialize
	 * @param contentType the content type to use for serialization
	 * @param values the values to serialize
	 * @return the serialized value as a stream
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> InputStream serializeAsStream(@Nonnull final String contentType, @Nonnull final Iterable<T> values) throws IOException
	{
		try(final SerializationWriter writer = getSerializationWriter(contentType, values)) {
			writer.writeCollectionOfObjectValues("", values);
			return writer.getSerializedContent();
		}
	}
	/**
	 * Serializes the given value to a string
	 * @param <T> the type of the value to serialize
	 * @param contentType the content type to use for serialization
	 * @param values the values to serialize
	 * @return the serialized value as a string
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> String serializeAsString(@Nonnull final String contentType, @Nonnull final Iterable<T> values) throws IOException {
		try(final InputStream stream = serializeAsStream(contentType, values)) {
			return new String(stream.readAllBytes(), CHARSET_NAME);
		}
	}
	private static SerializationWriter getSerializationWriter(@Nonnull final String contentType, @Nonnull final Object value) {
		Objects.requireNonNull(contentType);
		Objects.requireNonNull(value);
		if (contentType.isEmpty()) {
			throw new NullPointerException("content type cannot be empty");
		}
		return SerializationWriterFactoryRegistry.defaultInstance.getSerializationWriter(contentType);
	}
}
