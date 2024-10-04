package com.microsoft.kiota.serialization;

import com.microsoft.kiota.Compatibility;

import jakarta.annotation.Nonnull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

/**
 * Helper methods for serialization of kiota models
 */
public final class KiotaSerialization {
    private static final String CHARSET_NAME = "UTF-8";
    private static final boolean DEFAULT_SERIALIZE_ONLY_CHANGED_VALUES = true;

    private KiotaSerialization() {}

    /**
     * Serializes the given value to a stream
     * @param <T> the type of the value to serialize
     * @param contentType the content type to use for serialization
     * @param value the value to serialize
     * @return the serialized value as a stream
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static <T extends Parsable> InputStream serializeAsStream(
            @Nonnull final String contentType, @Nonnull final T value) throws IOException {
        return serializeAsStream(contentType, DEFAULT_SERIALIZE_ONLY_CHANGED_VALUES, value);
    }

    /**
     * Serializes the given value to a stream and configures returned values by the backing store if available
     * @param <T> the type of the value to serialize
     * @param contentType the content type to use for serialization
     * @param value the value to serialize
     * @param serializeOnlyChangedValues whether to serialize all values in value if value is a BackedModel
     * @return the serialized value as a stream
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static <T extends Parsable> InputStream serializeAsStream(
            @Nonnull final String contentType,
            final boolean serializeOnlyChangedValues,
            @Nonnull final T value)
            throws IOException {
        Objects.requireNonNull(value);
        try (final SerializationWriter writer =
                getSerializationWriter(contentType, serializeOnlyChangedValues)) {
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
    @Nonnull public static <T extends Parsable> String serializeAsString(
            @Nonnull final String contentType, @Nonnull final T value) throws IOException {
        Objects.requireNonNull(value);
        return serializeAsString(contentType, DEFAULT_SERIALIZE_ONLY_CHANGED_VALUES, value);
    }

    /**
     * Serializes the given value to a string
     * @param <T> the type of the value to serialize
     * @param contentType the content type to use for serialization
     * @param value the value to serialize
     * @param serializeOnlyChangedValues whether to serialize all values in value if value is a BackedModel
     * @return the serialized value as a string
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static <T extends Parsable> String serializeAsString(
            @Nonnull final String contentType,
            final boolean serializeOnlyChangedValues,
            @Nonnull final T value)
            throws IOException {
        Objects.requireNonNull(value);
        try (final InputStream stream =
                serializeAsStream(contentType, serializeOnlyChangedValues, value)) {
            return new String(Compatibility.readAllBytes(stream), CHARSET_NAME);
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
    @Nonnull public static <T extends Parsable> InputStream serializeAsStream(
            @Nonnull final String contentType, @Nonnull final Iterable<T> values)
            throws IOException {
        Objects.requireNonNull(values);
        return serializeAsStream(contentType, DEFAULT_SERIALIZE_ONLY_CHANGED_VALUES, values);
    }

    /**
     * Serializes the given value to a stream
     * @param <T> the type of the value to serialize
     * @param contentType the content type to use for serialization
     * @param values the values to serialize
     * @param serializeOnlyChangedValues whether to serialize all values in value if value is a BackedModel
     * @return the serialized value as a stream
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static <T extends Parsable> InputStream serializeAsStream(
            @Nonnull final String contentType,
            final boolean serializeOnlyChangedValues,
            @Nonnull final Iterable<T> values)
            throws IOException {
        Objects.requireNonNull(values);
        try (final SerializationWriter writer =
                getSerializationWriter(contentType, serializeOnlyChangedValues)) {
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
    @Nonnull public static <T extends Parsable> String serializeAsString(
            @Nonnull final String contentType, @Nonnull final Iterable<T> values)
            throws IOException {
        Objects.requireNonNull(values);
        return serializeAsString(contentType, DEFAULT_SERIALIZE_ONLY_CHANGED_VALUES, values);
    }

    /**
     * Serializes the given value to a string
     * @param <T> the type of the value to serialize
     * @param contentType the content type to use for serialization
     * @param values the values to serialize
     * @param serializeOnlyChangedValues whether to serialize all values in value if value is a BackedModel
     * @return the serialized value as a string
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static <T extends Parsable> String serializeAsString(
            @Nonnull final String contentType,
            final boolean serializeOnlyChangedValues,
            @Nonnull final Iterable<T> values)
            throws IOException {
        Objects.requireNonNull(values);
        try (final InputStream stream =
                serializeAsStream(contentType, serializeOnlyChangedValues, values)) {
            return new String(Compatibility.readAllBytes(stream), CHARSET_NAME);
        }
    }

    private static SerializationWriter getSerializationWriter(
            @Nonnull final String contentType, final boolean serializeOnlyChangedValues) {
        Objects.requireNonNull(contentType);
        if (contentType.isEmpty()) {
            throw new NullPointerException("content type cannot be empty");
        }
        return SerializationWriterFactoryRegistry.defaultInstance.getSerializationWriter(
                contentType, serializeOnlyChangedValues);
    }

    /**
     * Deserializes the given stream to a model object
     * @param <T> the type of the value to deserialize
     * @param contentType the content type to use for deserialization
     * @param stream the stream to deserialize
     * @param parsableFactory the factory to use for creating the model object
     * @return the deserialized value
     */
    @Nonnull public static <T extends Parsable> T deserialize(
            @Nonnull final String contentType,
            @Nonnull final InputStream stream,
            @Nonnull final ParsableFactory<T> parsableFactory) {
        final ParseNode parseNode = getRootParseNode(contentType, stream, parsableFactory);
        return parseNode.getObjectValue(parsableFactory);
    }

    private static <T extends Parsable> ParseNode getRootParseNode(
            @Nonnull final String contentType,
            @Nonnull final InputStream stream,
            @Nonnull final ParsableFactory<T> parsableFactory) {
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(stream);
        Objects.requireNonNull(parsableFactory);
        if (contentType.isEmpty()) {
            throw new NullPointerException("content type cannot be empty");
        }
        return ParseNodeFactoryRegistry.defaultInstance.getParseNode(contentType, stream);
    }

    private static InputStream getInputStreamFromString(@Nonnull final String value)
            throws UnsupportedEncodingException {
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
    @Nonnull public static <T extends Parsable> T deserialize(
            @Nonnull final String contentType,
            @Nonnull final String value,
            @Nonnull final ParsableFactory<T> parsableFactory)
            throws IOException {
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
    @Nonnull public static <T extends Parsable> List<T> deserializeCollection(
            @Nonnull final String contentType,
            @Nonnull final String value,
            @Nonnull final ParsableFactory<T> parsableFactory)
            throws IOException {
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
    @Nonnull public static <T extends Parsable> List<T> deserializeCollection(
            @Nonnull final String contentType,
            @Nonnull final InputStream stream,
            @Nonnull final ParsableFactory<T> parsableFactory) {
        final ParseNode parseNode = getRootParseNode(contentType, stream, parsableFactory);
        return parseNode.getCollectionOfObjectValues(parsableFactory);
    }
}
