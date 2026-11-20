package com.microsoft.kiota.serialization;

import com.microsoft.kiota.ClientException;

import jakarta.annotation.Nonnull;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This factory holds a list of all the registered factories for the various types of nodes.
 */
public class ParseNodeFactoryRegistry implements ParseNodeFactory {
    /** Default constructor for the registry. */
    public ParseNodeFactoryRegistry() {
        // Default constructor for the registry.
    }

    /** Default singleton instance of the registry to be used when registering new factories that should be available by default. */
    public static final ParseNodeFactoryRegistry defaultInstance = new ParseNodeFactoryRegistry();

    /** List of factories that are registered by content type. */
    @Nonnull public final Map<String, ParseNodeFactory> contentTypeAssociatedFactories =
            new CaseInsensitiveMap<>();

    @Nonnull public String getValidContentType() {
        throw new UnsupportedOperationException(
                "The registry supports multiple content types. Get the registered factory"
                        + " instead.");
    }

    private static final Pattern contentTypeVendorCleanupPattern =
            Pattern.compile("[^/]+\\+", Pattern.CASE_INSENSITIVE);

    /** {@inheritDoc}
     * @throws ClientException when no factory is registered for the content type
     */
    @Override
    @Nonnull public ParseNode getParseNode(
            @Nonnull final String contentType, @Nonnull final InputStream rawResponse) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        Objects.requireNonNull(rawResponse, "parameter rawResponse cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        }
        final String vendorSpecificContentType =
                contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (vendorSpecificContentType.isEmpty()) {
            throw new ClientException(
                    "Content type " + contentType + " does not have a factory to be parsed");
        }
        ParseNodeFactory factory = contentTypeAssociatedFactories.get(vendorSpecificContentType);
        if (factory != null) {
            return factory.getParseNode(vendorSpecificContentType, rawResponse);
        }
        final String cleanedContentType =
                contentTypeVendorCleanupPattern.matcher(vendorSpecificContentType).replaceAll("");
        factory = contentTypeAssociatedFactories.get(cleanedContentType);
        if (factory != null) {
            return factory.getParseNode(cleanedContentType, rawResponse);
        }
        throw new ClientException(
                "Content type " + cleanedContentType + " does not have a factory to be parsed");
    }
}
