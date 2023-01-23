package com.microsoft.kiota.serialization;

import java.util.Objects;

import javax.annotation.Nonnull;

/** Creates instances of Form Serialization Writers */
public class FormSerializationWriterFactory implements SerializationWriterFactory {
    /** Instantiates a new factory */
    public FormSerializationWriterFactory() {
    }
    @Nonnull
    public String getValidContentType() {
        return validContentType;
    }
    private static final String validContentType = "application/x-www-form-urlencoded";
    @Override
    @Nonnull
    public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if(contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        return new FormSerializationWriter();
    }
}
