package com.microsoft.kiota.serialization;

import com.google.gson.Gson;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Creates new Json serialization writers. */
public class JsonSerializationWriterFactory implements SerializationWriterFactory {
    private final Gson gson;

    /** Creates a new factory */
    public JsonSerializationWriterFactory() {
        this(DefaultGsonBuilder.getDefaultInstance());
    }

    /**
     * Creates a new factory
     * @param gson the {@link Gson} instance to use for writing value types.
     */
    public JsonSerializationWriterFactory(@Nonnull Gson gson) {
        Objects.requireNonNull(gson, "gson contentType cannot be null");
        this.gson = gson;
    }

    /** {@inheritDoc} */
    @Nonnull public String getValidContentType() {
        return validContentType;
    }

    private static final String validContentType = "application/json";

    /** {@inheritDoc} */
    @Override
    @Nonnull public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        return new JsonSerializationWriter(gson);
    }
}
