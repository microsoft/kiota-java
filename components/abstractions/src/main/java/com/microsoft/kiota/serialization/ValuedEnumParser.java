package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** The interface for a valued enum parser. */
@FunctionalInterface
public interface ValuedEnumParser<T extends Enum> {
    /**
     * Gets an enum from it's string value.
     * @param value the string value of the enum.
     * @return the enum value derived from the string.
     */
    @Nullable T forValue(@Nonnull String value);
}
