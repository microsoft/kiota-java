package com.microsoft.kiota.store;

import jakarta.annotation.Nonnull;

/** This class is used to register the backing store factory. */
public class BackingStoreFactorySingleton {
    /** Default constructor */
    public BackingStoreFactorySingleton() {}
    /** The backing store factory singleton instance. */
    @Nonnull
    public static BackingStoreFactory instance = new InMemoryBackingStoreFactory();
}