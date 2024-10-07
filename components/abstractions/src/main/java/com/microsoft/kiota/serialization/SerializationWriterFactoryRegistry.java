package com.microsoft.kiota.serialization;

import com.microsoft.kiota.store.BackingStoreSerializationWriterProxyFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
        return getSerializationWriter(contentType, true);
    }

    /**
     * Get a Serialization Writer with backing store configured with serializeOnlyChangedValues
     * @param contentType
     * @param serializeOnlyChangedValues control backing store functionality
     * @return the serialization writer
     * @throws RuntimeException when no factory is found for content type
     */
    @Nonnull public SerializationWriter getSerializationWriter(
            @Nonnull final String contentType, final boolean serializeOnlyChangedValues) {
        Objects.requireNonNull(contentType, "parameter contentType cannot be null");
        if (contentType.isEmpty()) {
            throw new NullPointerException("contentType cannot be empty");
        }
        String cleanedContentType = getVendorSpecificContentType(contentType);
        SerializationWriterFactory factory = getSerializationWriterFactory(cleanedContentType);
        if (factory == null) {
            cleanedContentType = getCleanedVendorSpecificContentType(cleanedContentType);
            factory =
                    getSerializationWriterFactory(
                            getCleanedVendorSpecificContentType(cleanedContentType));
            if (factory == null) {
                throw new RuntimeException(
                        "Content type "
                                + contentType
                                + " does not have a factory to be serialized");
            }
        }
        if (!serializeOnlyChangedValues) {
            if (factory instanceof BackingStoreSerializationWriterProxyFactory) {
                return ((BackingStoreSerializationWriterProxyFactory) factory)
                        .getSerializationWriter(cleanedContentType, serializeOnlyChangedValues);
            }
        }
        return factory.getSerializationWriter(cleanedContentType);
    }

    /**
     * Gets a SerializationWriterFactory that is mapped to a cleaned content type string
     * @param contentType wrapper object carrying initial content type and result of parsing it
     * @return the serialization writer factory or null if no mapped factory is found
     */
    @Nullable private SerializationWriterFactory getSerializationWriterFactory(
            @Nonnull final String contentType) {
        if (contentTypeAssociatedFactories.containsKey(contentType)) {
            return contentTypeAssociatedFactories.get(contentType);
        }
        return null;
    }

    /**
     * Splits content type by ; and returns first segment or original contentType
     * @param contentType
     * @return vendor specific content type
     */
    @Nonnull private String getVendorSpecificContentType(@Nonnull final String contentType) {
        String[] split = contentType.split(";");
        if (split.length >= 1) {
            return split[0];
        }
        return contentType;
    }

    /**
     * Does a regex match on the content type replacing special characters
     * @param contentType
     * @return cleaned content type
     */
    @Nonnull private String getCleanedVendorSpecificContentType(@Nonnull final String contentType) {
        return contentTypeVendorCleanupPattern.matcher(contentType).replaceAll("");
    }
}
