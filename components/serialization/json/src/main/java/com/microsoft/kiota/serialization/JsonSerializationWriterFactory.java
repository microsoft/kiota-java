package com.microsoft.kiota.serialization;

import com.google.gson.Gson;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Creates new Json serialization writers. */
public class JsonSerializationWriterFactory implements SerializationWriterFactory {
    /** Creates a new factory */
    public JsonSerializationWriterFactory() {}

    /** {@inheritDoc} */
    @Nonnull public String getValidContentType() {
        return validContentType;
    }

    private static final String validContentType = "application/json";

    private Gson gson = DefaultGsonBuilder.getDefaultInstance();

    /**
     * @return the {@link Gson} instance to use for writing value types.
     */
    @Nonnull public Gson getGson() {
        return gson;
    }

    /**
     * Specify a custom {@link Gson} instance for writing value types.
     * @param gson the {@link Gson} instance to use.
     */
    public void setGson(@Nonnull Gson gson) {
        this.gson = gson;
    }

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
