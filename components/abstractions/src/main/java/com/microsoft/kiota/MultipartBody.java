package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.serialization.SerializationWriterFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a multipart body for a request or a response.
 */
public class MultipartBody implements Parsable {
    /**
     * Creates a new instance of the MultipartBody class.
     */
    public MultipartBody() {
        // default empty constructor
    }

    @Nonnull private final String boundary = UUID.randomUUID().toString().replace("-", "");

    /**
     * Gets the boundary string for the multipart body.
     * @return the boundary string for the multipart body.
     */
    @Nonnull public String getBoundary() {
        return boundary;
    }

    /**
     * The request adapter to use for the multipart body serialization.
     */
    @Nullable public RequestAdapter requestAdapter;

    /**
     * Adds or replaces a part in the multipart body.
     * @param <T> the type of the part to add or replace.
     * @param name the name of the part to add or replace.
     * @param contentType the content type of the part to add or replace.
     * @param value the value of the part to add or replace.
     */
    public <T> void addOrReplacePart(
            @Nonnull final String name, @Nonnull final String contentType, @Nonnull final T value) {
        addOrReplacePart(name, contentType, value, null);
    }

    /**
     * Adds or replaces a part in the multipart body.
     *
     * @param <T> the type of the part to add or replace.
     * @param name the name of the part to add or replace.
     * @param contentType the content type of the part to add or replace.
     * @param value the value of the part to add or replace.
     * @param filename the value of the filename directive.
     */
    public <T> void addOrReplacePart(
            @Nonnull final String name,
            @Nonnull final String contentType,
            @Nonnull final T value,
            @Nullable String filename) {
        Objects.requireNonNull(value);
        if (Compatibility.isBlank(contentType))
            throw new IllegalArgumentException("contentType cannot be blank or empty");
        if (Compatibility.isBlank(name))
            throw new IllegalArgumentException("name cannot be blank or empty");

        final String normalizedName = normalizePartName(name);
        Part part = new Part(name, value, contentType, filename);
        parts.put(normalizedName, part);
    }

    private final Map<String, Part> parts = new HashMap<>();

    private String normalizePartName(@Nonnull final String original) {
        return original.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the content type of the part with the specified name.
     * @param partName the name of the part to get.
     * @return the content type of the part with the specified name.
     */
    @Nullable public Object getPartValue(@Nonnull final String partName) {
        if (Compatibility.isBlank(partName))
            throw new IllegalArgumentException("partName cannot be blank or empty");
        final String normalizedName = normalizePartName(partName);
        final Part candidate = parts.get(normalizedName);
        if (candidate == null) return null;
        return candidate.getValue();
    }

    /**
     * Gets the content type of the part with the specified name.
     * @param partName the name of the part to get.
     * @return the content type of the part with the specified name.
     */
    public boolean removePart(@Nonnull final String partName) {
        if (Compatibility.isBlank(partName))
            throw new IllegalArgumentException("partName cannot be blank or empty");
        final String normalizedName = normalizePartName(partName);
        final Object candidate = parts.remove(normalizedName);
        return candidate != null;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        throw new UnsupportedOperationException("Unimplemented method 'getFieldDeserializers'");
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(@Nonnull final SerializationWriter writer) {
        Objects.requireNonNull(writer);
        final RequestAdapter ra = requestAdapter;
        if (ra == null) throw new IllegalStateException("requestAdapter cannot be null");
        if (parts.isEmpty()) throw new IllegalStateException("multipart body cannot be empty");
        final SerializationWriterFactory serializationFactory = ra.getSerializationWriterFactory();
        boolean isFirst = true;
        for (final Map.Entry<String, Part> partEntry : parts.entrySet()) {
            try {
                Part part = partEntry.getValue();
                if (isFirst) isFirst = false;
                else writer.writeStringValue("", "");
                writer.writeStringValue("", "--" + getBoundary());
                final String partContentType = part.getContentType();
                writer.writeStringValue("Content-Type", partContentType);

                String contentDisposition = "form-data; name=\"" + part.getName() + "\"";
                if (part.getFilename() != null && !part.getFilename().trim().isEmpty()) {
                    contentDisposition += "; filename=\"" + part.getFilename() + "\"";
                }
                writer.writeStringValue("Content-Disposition", contentDisposition);

                writer.writeStringValue("", "");
                final Object objectValue = part.getValue();
                if (objectValue instanceof Parsable) {
                    try (final SerializationWriter partWriter =
                            serializationFactory.getSerializationWriter(partContentType)) {
                        partWriter.writeObjectValue("", ((Parsable) objectValue));
                        try (final InputStream partContent = partWriter.getSerializedContent()) {
                            if (partContent.markSupported()) partContent.reset();
                            writer.writeByteArrayValue("", Compatibility.readAllBytes(partContent));
                        }
                    }
                } else if (objectValue instanceof String) {
                    writer.writeStringValue("", (String) objectValue);
                } else if (objectValue instanceof InputStream) {
                    final InputStream inputStream = (InputStream) objectValue;
                    if (inputStream.markSupported()) inputStream.reset();
                    writer.writeByteArrayValue("", Compatibility.readAllBytes(inputStream));
                } else if (objectValue instanceof byte[]) {
                    writer.writeByteArrayValue("", (byte[]) objectValue);
                } else {
                    throw new IllegalStateException(
                            "Unsupported part type" + objectValue.getClass().getName());
                }
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        writer.writeStringValue("", "");
        writer.writeStringValue("", "--" + boundary + "--");
    }

    private class Part {
        private final String name;
        private final Object value;
        private final String contentType;
        private final String filename;

        Part(String name, Object value, String contentType, String filename) {
            this.name = name;
            this.value = value;
            this.contentType = contentType;
            this.filename = filename;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public String getContentType() {
            return contentType;
        }

        public String getFilename() {
            return filename;
        }
    }
}
