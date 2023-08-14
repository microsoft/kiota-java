package com.microsoft.kiota.serialization;

import java.util.Objects;

import jakarta.annotation.Nonnull;

/** Creates new text serialization writers */
public class TextSerializationWriterFactory implements SerializationWriterFactory {
    /** Creates a new instance of the factory */
    public TextSerializationWriterFactory() {}
    @Nonnull
    public String getValidContentType() {
        return validContentType;
    }
    private static final String validContentType = "text/plain";
    @Override
    @Nonnull
    public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if(contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        return new TextSerializationWriter();
    }
}
