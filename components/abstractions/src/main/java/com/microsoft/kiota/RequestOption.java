package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

/** Represents a request option. */
public interface RequestOption {
    /**
     * Gets the type of the option to use for middleware retrieval.
     * @param <T> Type of the option.
     * @return Class of the option type.
     */
    @Nonnull
    public <T extends RequestOption> Class<T> getType();
}
