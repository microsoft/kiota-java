package com.microsoft.kiota.store;

import javax.annotation.Nonnull;

/** This class is used to register the backing store factory. */
public class BackingStoreFactorySingleton {
    /** The backing store factory singleton instance. */
    @Nonnull
    public static BackingStoreFactory instance = new InMemoryBackingStoreFactory();
}