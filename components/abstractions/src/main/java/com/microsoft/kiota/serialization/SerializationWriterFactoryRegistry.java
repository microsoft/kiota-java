package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

/** This factory holds a list of all the registered factories for the various types of nodes. */
public class SerializationWriterFactoryRegistry implements SerializationWriterFactory {
    /** Default constructor for the registry. */
    public SerializationWriterFactoryRegistry() {
        // Default constructor for the registry.
    }

    /** Default singleton instance of the registry to be used when registering new factories that should be available by default. */
    public static final SerializationWriterFactoryRegistry defaultInstance =
            new SerializationWriterFactoryRegistry();

    /** List of factories that are registered by content type. */
    @Nonnull public final HashMap<String, SerializationWriterFactory> contentTypeAssociatedFactories =
            new HashMap<>();

    @Nonnull public String getValidContentType() {
        throw new UnsupportedOperationException(
                "The registry supports multiple content types. Get the registered factory"
                        + " instead.");
    }

    private static final Pattern contentTypeVendorCleanupPattern =
            Pattern.compile("[^/]+\\+", Pattern.CASE_INSENSITIVE);

    @Override
    @Nonnull public SerializationWriter getSerializationWriter(@Nonnull final String contentType) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        }
        final String vendorSpecificContentType = contentType.split(";")[0];
        if (contentTypeAssociatedFactories.containsKey(vendorSpecificContentType)) {
            return contentTypeAssociatedFactories
                    .get(vendorSpecificContentType)
                    .getSerializationWriter(vendorSpecificContentType);
        }
        final String cleanedContentType =
                contentTypeVendorCleanupPattern.matcher(vendorSpecificContentType).replaceAll("");
        if (contentTypeAssociatedFactories.containsKey(cleanedContentType)) {
            return contentTypeAssociatedFactories
                    .get(cleanedContentType)
                    .getSerializationWriter(cleanedContentType);
        }
        throw new RuntimeException(
                "Content type " + contentType + " does not have a factory to be serialized");
    }
}
