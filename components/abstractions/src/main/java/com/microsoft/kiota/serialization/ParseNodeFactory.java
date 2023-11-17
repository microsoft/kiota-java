package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;
import java.io.InputStream;

/**
 * Defines the contract for a factory that is used to create {@link ParseNode}s.
 */
public interface ParseNodeFactory {
    /**
     * Returns the content type this factory's parse nodes can deserialize.
     * @return the content type this factory's parse nodes can deserialize.
     */
    @Nonnull String getValidContentType();

    /**
     * Creates a {@link ParseNode} from the given {@link InputStream} and content type.
     * @param rawResponse the {@link InputStream} to read from.
     * @param contentType the content type of the {@link InputStream}.
     * @return a {@link ParseNode} that can deserialize the given {@link InputStream}.
     */
    @Nonnull ParseNode getParseNode(
            @Nonnull final String contentType, @Nonnull final InputStream rawResponse);
}
