package com.microsoft.kiota.serialization;

import com.microsoft.kiota.store.BackingStoreSerializationWriterProxyFactory;

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
        final ContentTypeWrapper contentTypeWrapper = new ContentTypeWrapper(contentType);
        final SerializationWriterFactory serializationWriterFactory =
                getSerializationWriterFactory(contentTypeWrapper);
        return serializationWriterFactory.getSerializationWriter(
                contentTypeWrapper.cleanedContentType);
    }

    /**
     * Get a Serialization Writer with backing store configured with serializeOnlyChangedValues
     * @param contentType
     * @param serializeOnlyChangedValues control backing store functionality
     * @return the serialization writer
     */
    @Nonnull public SerializationWriter getSerializationWriter(
            @Nonnull final String contentType, final boolean serializeOnlyChangedValues) {
        if (!serializeOnlyChangedValues) {
            final ContentTypeWrapper contentTypeWrapper = new ContentTypeWrapper(contentType);
            final SerializationWriterFactory factory =
                    getSerializationWriterFactory(contentTypeWrapper);
            if (factory instanceof BackingStoreSerializationWriterProxyFactory) {
                return ((BackingStoreSerializationWriterProxyFactory) factory)
                        .getSerializationWriter(
                                contentTypeWrapper.cleanedContentType, serializeOnlyChangedValues);
            }
        }
        return getSerializationWriter(contentType);
    }

    /**
     * Gets a SerializationWriterFactory that is mapped to a cleaned content type string
     * @param contentTypeWrapper wrapper object carrying initial content type and result of parsing it
     * @return the serialization writer factory
     * @throws RuntimeException when no mapped factory is found
     */
    @Nonnull private SerializationWriterFactory getSerializationWriterFactory(
            @Nonnull final ContentTypeWrapper contentTypeWrapper) {
        final String vendorSpecificContentType =
                getVendorSpecificContentType(contentTypeWrapper.contentType);
        if (contentTypeAssociatedFactories.containsKey(vendorSpecificContentType)) {
            contentTypeWrapper.cleanedContentType = vendorSpecificContentType;
            return contentTypeAssociatedFactories.get(contentTypeWrapper.cleanedContentType);
        }
        final String cleanedContentType =
                getCleanedVendorSpecificContentType(vendorSpecificContentType);
        if (contentTypeAssociatedFactories.containsKey(cleanedContentType)) {
            contentTypeWrapper.cleanedContentType = cleanedContentType;
            return contentTypeAssociatedFactories.get(contentTypeWrapper.cleanedContentType);
        }
        throw new RuntimeException(
                "Content type "
                        + contentTypeWrapper.contentType
                        + " does not have a factory to be serialized");
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

    /**
     * Wrapper class to carry the cleaned version of content-type after parsing in multiple stages
     */
    private static final class ContentTypeWrapper {
        String contentType;
        String cleanedContentType;

        ContentTypeWrapper(@Nonnull final String contentType) {
            this.contentType = contentType;
            this.cleanedContentType = "";
        }
    }
}
