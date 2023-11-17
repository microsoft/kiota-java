package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

/** Creates instances of Form Parse Nodes */
public class FormParseNodeFactory implements ParseNodeFactory {
    /** Instantiates a new factory */
    public FormParseNodeFactory() {}

    @Nonnull public String getValidContentType() {
        return validContentType;
    }

    private static final String validContentType = "application/x-www-form-urlencoded";

    @Override
    @Nonnull public ParseNode getParseNode(
            @Nonnull final String contentType, @Nonnull final InputStream rawResponse) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        Objects.requireNonNull(rawResponse, "parameter rawResponse cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        } else if (!contentType.equals(validContentType)) {
            throw new IllegalArgumentException("expected a " + validContentType + " content type");
        }
        String rawText;
        try (final InputStreamReader reader =
                new InputStreamReader(rawResponse, StandardCharsets.UTF_8)) {
            try (final BufferedReader buff = new BufferedReader(reader)) {
                rawText = buff.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not close the reader", ex);
        }
        return new FormParseNode(rawText);
    }
}
