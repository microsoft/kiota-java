package com.microsoft.kiota.serialization;
/**
 * Serialization helpers for kiota models.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Nonnull;
/**
 * Helper methods for serialization of kiota models
 */
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
	/**
	 * Deserializes the given stream to a model object
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param stream the stream to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String contentType, @Nonnull final InputStream stream, @Nonnull final ParsableFactory<T> parsableFactory) {
		final ParseNode parseNode = getRootParseNode(contentType, stream, parsableFactory);
		return parseNode.getObjectValue(parsableFactory);
	}
	private static <T extends Parsable> ParseNode getRootParseNode(@Nonnull final String contentType, @Nonnull final InputStream stream, @Nonnull final ParsableFactory<T> parsableFactory) {
		Objects.requireNonNull(contentType);
		Objects.requireNonNull(stream);
		Objects.requireNonNull(parsableFactory);
		if (contentType.isEmpty()) {
			throw new NullPointerException("content type cannot be empty");
		}
		return ParseNodeFactoryRegistry.defaultInstance.getParseNode(contentType, stream);
	}
	private static InputStream getInputStreamFromString(@Nonnull final String value) throws UnsupportedEncodingException {
		Objects.requireNonNull(value);
		return new ByteArrayInputStream(value.getBytes(CHARSET_NAME));
	}
	/**
	 * Deserializes the given string to a model object
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param value the string to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String contentType, @Nonnull final String value, @Nonnull final ParsableFactory<T> parsableFactory) throws IOException {
		try (final InputStream stream = getInputStreamFromString(value)) {
			return deserialize(contentType, stream, parsableFactory);
		}
	}
	/**
	 * Deserializes the given string to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param value the string to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String contentType, @Nonnull final String value, @Nonnull final ParsableFactory<T> parsableFactory) throws IOException {
		try (final InputStream stream = getInputStreamFromString(value)) {
			return deserializeCollection(contentType, stream, parsableFactory);
		}
	}
	/**
	 * Deserializes the given stream to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param stream the stream to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String contentType, @Nonnull final InputStream stream, @Nonnull final ParsableFactory<T> parsableFactory) {
		final ParseNode parseNode = getRootParseNode(contentType, stream, parsableFactory);
		return parseNode.getCollectionOfObjectValues(parsableFactory);
	}
	/**
	 * Deserializes the given stream to a model object
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param stream the stream to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String contentType, @Nonnull final InputStream stream, @Nonnull final Class<T> typeClass) {
		return deserialize(contentType, stream, getFactoryMethodFromType(typeClass));
	}
	/**
	 * Deserializes the given string to a model object
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param value the string to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String contentType, @Nonnull final String value, @Nonnull final Class<T> typeClass) throws IOException {
		return deserialize(contentType, value, getFactoryMethodFromType(typeClass));
	}
	/**
	 * Deserializes the given stream to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param stream the stream to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String contentType, @Nonnull final InputStream stream, @Nonnull final Class<T> typeClass) {
		return deserializeCollection(contentType, stream, getFactoryMethodFromType(typeClass));
	}
	/**
	 * Deserializes the given string to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param contentType the content type to use for deserialization
	 * @param value the string to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String contentType, @Nonnull final String value, @Nonnull final Class<T> typeClass) throws IOException {
		return deserializeCollection(contentType, value, getFactoryMethodFromType(typeClass));
	}
	@Nonnull
	@SuppressWarnings("unchecked")
	private static <T extends Parsable> ParsableFactory<T> getFactoryMethodFromType(@Nonnull final Class<T> type) {
		Objects.requireNonNull(type);
		try {
			final Method method = type.getMethod("createFromDiscriminatorValue", ParseNode.class);
			return node -> {
				try {
					return (T)method.invoke(null, node);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			};
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
