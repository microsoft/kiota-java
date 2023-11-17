package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/** The interface for the valued enum. */
public interface ValuedEnum {
    /**
     * Gets the string representation of the enum value.
     * @return the string representation of the enum value.
     */
    @Nonnull String getValue();
}
