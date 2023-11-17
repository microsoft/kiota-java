package com.microsoft.kiota.store;

import jakarta.annotation.Nonnull;

/** This class is used to create instances of InMemoryBackingStore */
public class InMemoryBackingStoreFactory implements BackingStoreFactory {
    /** Creates a new instance of the factory */
    public InMemoryBackingStoreFactory() {
        // default constructor
    }

    @Override
    @Nonnull public BackingStore createBackingStore() {
        return new InMemoryBackingStore();
    }
}
