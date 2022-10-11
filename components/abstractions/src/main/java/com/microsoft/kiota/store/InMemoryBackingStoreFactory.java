package com.microsoft.kiota.store;

import javax.annotation.Nonnull;

/** This class is used to create instances of InMemoryBackingStore */
public class InMemoryBackingStoreFactory implements BackingStoreFactory {
    @Override
    @Nonnull
    public BackingStore createBackingStore() {
        return new InMemoryBackingStore();
    }
}
