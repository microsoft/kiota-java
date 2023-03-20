package com.microsoft.kiota.serialization;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.annotation.Nonnull;

/** Creates new Json parse nodes from the payload. */
public class JsonParseNodeFactory implements ParseNodeFactory {
    /** Creates a new factory */
    public JsonParseNodeFactory() {}
    /** {@inheritDoc} */
    @Nonnull
    public String getValidContentType() {
        return validContentType;
    }
    private final static String validContentType = "application/json";
    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ParseNode getParseNode(@Nonnull final String contentType, @Nonnull final InputStream rawResponse) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        Objects.requireNonNull(rawResponse, "parameter rawResponse cannot be null");
        if(contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        try(final InputStreamReader reader = new InputStreamReader(rawResponse, StandardCharsets.UTF_8)) {
            return new JsonParseNode(JsonParser.parseReader(reader));
        } catch (IOException ex) {
            throw new RuntimeException("could not close the reader", ex);
        }
    }

}