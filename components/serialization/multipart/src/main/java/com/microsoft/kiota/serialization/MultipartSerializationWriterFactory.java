package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Creates instances of Multipart Serialization Writers */
public class MultipartSerializationWriterFactory implements SerializationWriterFactory {
    /** Instantiates a new factory */
    public MultipartSerializationWriterFactory() {}

    @Nonnull public String getValidContentType() {
        return validContentType;
    }

    private static final String validContentType = "multipart/form-data";

    @Override
    @Nonnull public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        return new MultipartSerializationWriter();
    }
}
