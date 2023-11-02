package com.microsoft.kiota.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jakarta.annotation.Nonnull;

/**
 * Helper methods for JSON serialization of kiota models
 */
public class KiotaJsonSerialization {
	private static final String CONTENT_TYPE = "application/json";
	private KiotaJsonSerialization() {}
	/**
	 * Serializes the given value to a stream
	 * @param <T> the type of the value to serialize
	 * @param value the value to serialize
	 * @return the serialized value as a stream
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> InputStream serializeAsStream(@Nonnull final T value) throws IOException {
		return KiotaSerialization.serializeAsStream(CONTENT_TYPE, value);
	}
	/**
	 * Serializes the given value to a string
	 * @param <T> the type of the value to serialize
	 * @param value the value to serialize
	 * @return the serialized value as a string
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> String serializeAsString(@Nonnull final T value) throws IOException {
		return KiotaSerialization.serializeAsString(CONTENT_TYPE, value);
	}
	/**
	 * Serializes the given value to a stream
	 * @param <T> the type of the value to serialize
	 * @param values the values to serialize
	 * @return the serialized value as a stream
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> InputStream serializeAsStream(@Nonnull final Iterable<T> values) throws IOException {
		return KiotaSerialization.serializeAsStream(CONTENT_TYPE, values);
	}
	/**
	 * Serializes the given value to a string
	 * @param <T> the type of the value to serialize
	 * @param values the values to serialize
	 * @return the serialized value as a string
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> String serializeAsString(@Nonnull final Iterable<T> values) throws IOException {
		return KiotaSerialization.serializeAsString(CONTENT_TYPE, values);
	}
	/**
	 * Deserializes the given stream to a model object
	 * @param <T> the type of the value to deserialize
	 * @param stream the stream to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final InputStream stream, @Nonnull final ParsableFactory<T> parsableFactory) {
		return KiotaSerialization.deserialize(CONTENT_TYPE, stream, parsableFactory);
	}
	/**
	 * Deserializes the given string to a model object
	 * @param <T> the type of the value to deserialize
	 * @param value the string to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String value, @Nonnull final ParsableFactory<T> parsableFactory) throws IOException {
		return KiotaSerialization.deserialize(CONTENT_TYPE, value, parsableFactory);
	}
	/**
	 * Deserializes the given string to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param value the string to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String value, @Nonnull final ParsableFactory<T> parsableFactory) throws IOException {
		return KiotaSerialization.deserializeCollection(CONTENT_TYPE, value, parsableFactory);
	}
	/**
	 * Deserializes the given stream to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param stream the stream to deserialize
	 * @param parsableFactory the factory to use for creating the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final InputStream stream, @Nonnull final ParsableFactory<T> parsableFactory) {
		return KiotaSerialization.deserializeCollection(CONTENT_TYPE, stream, parsableFactory);
	}
	/**
	 * Deserializes the given stream to a model object
	 * @param <T> the type of the value to deserialize
	 * @param stream the stream to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final InputStream stream, @Nonnull final Class<T> typeClass) {
		return KiotaSerialization.deserialize(CONTENT_TYPE, stream, typeClass);
	}
	/**
	 * Deserializes the given string to a model object
	 * @param <T> the type of the value to deserialize
	 * @param value the string to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> T deserialize(@Nonnull final String value, @Nonnull final Class<T> typeClass) throws IOException {
		return KiotaSerialization.deserialize(CONTENT_TYPE, value, typeClass);
	}
	/**
	 * Deserializes the given stream to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param stream the stream to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final InputStream stream, @Nonnull final Class<T> typeClass) {
		return KiotaSerialization.deserializeCollection(CONTENT_TYPE, stream, typeClass);
	}
	/**
	 * Deserializes the given string to a collection of model objects
	 * @param <T> the type of the value to deserialize
	 * @param value the string to deserialize
	 * @param typeClass the class of the model object
	 * @return the deserialized value
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static <T extends Parsable> List<T> deserializeCollection(@Nonnull final String value, @Nonnull final Class<T> typeClass) throws IOException {
		return KiotaSerialization.deserializeCollection(CONTENT_TYPE, value, typeClass);
	}
}
