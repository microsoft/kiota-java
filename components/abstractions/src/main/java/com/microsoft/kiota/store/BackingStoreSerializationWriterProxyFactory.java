package com.microsoft.kiota.store;

import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.SerializationWriterProxyFactory;

import jakarta.annotation.Nonnull;

/**Proxy implementation of SerializationWriterFactory for the backing store that automatically sets the state of the backing store when serializing. */
public class BackingStoreSerializationWriterProxyFactory extends SerializationWriterProxyFactory {
    /**
     * Initializes a new instance of the BackingStoreSerializationWriterProxyFactory class given a concrete implementation of SerializationWriterFactory.
     * @param concrete a concrete implementation of SerializationWriterFactory to wrap.
     */
    public BackingStoreSerializationWriterProxyFactory(
            @Nonnull final SerializationWriterFactory concrete) {
        super(
                concrete,
                (x) -> {
                    if (x instanceof BackedModel) {
                        final BackedModel backedModel = (BackedModel) x;
                        final BackingStore backingStore = backedModel.getBackingStore();
                        if (backingStore != null) {
                            backingStore.setReturnOnlyChangedValues(true);
                        }
                    }
                },
                (x) -> {
                    if (x instanceof BackedModel) {
                        final BackedModel backedModel = (BackedModel) x;
                        final BackingStore backingStore = backedModel.getBackingStore();
                        if (backingStore != null) {
                            backingStore.setReturnOnlyChangedValues(false);
                            backingStore.setIsInitializationCompleted(true);
                        }
                    }
                },
                (x, y) -> {
                    if (x instanceof BackedModel) {
                        final BackedModel backedModel = (BackedModel) x;
                        final BackingStore backingStore = backedModel.getBackingStore();
                        if (backingStore != null) {
                            final Iterable<String> keys =
                                    backingStore.enumerateKeysForValuesChangedToNull();
                            for (final String key : keys) {
                                y.writeNullValue(key);
                            }
                        }
                    }
                });
    }

    /**
     * Returns a SerializationWriter that overrides the default serialization of only changed values if serializeOnlyChangedValues="true"
     * Gets the previously proxied serialization writer without any backing store configuration to prevent overwriting the registry affecting
     * future serialization requests
     *
     * @param contentType HTTP content type header value
     * @param serializeOnlyChangedValues alter backing store default behavior
     * @return the SerializationWriter
     */
    @Nonnull public SerializationWriter getSerializationWriter(
            @Nonnull final String contentType, final boolean serializeOnlyChangedValues) {
        if (!serializeOnlyChangedValues) {
            return _concrete.getSerializationWriter(contentType);
        }
        return getSerializationWriter(contentType);
    }
}
