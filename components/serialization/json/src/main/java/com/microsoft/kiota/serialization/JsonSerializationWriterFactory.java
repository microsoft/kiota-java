package com.microsoft.kiota.serialization;

import java.util.Objects;

import javax.annotation.Nonnull;

/** Creates new Json serialization writers. */
public class JsonSerializationWriterFactory implements SerializationWriterFactory {
    /** Creates a new factory */
    public JsonSerializationWriterFactory() {}
    /** {@inheritDoc} */
    @Nonnull
    public String getValidContentType() {
        return validContentType;
    }
    private static final String validContentType = "application/json";
    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if(contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        return new JsonSerializationWriter();
    }
}
