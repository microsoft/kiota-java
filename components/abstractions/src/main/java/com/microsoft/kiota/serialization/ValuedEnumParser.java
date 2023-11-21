package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/** The interface for a valued enum parser. */
public interface ValuedEnumParser<T extends Enum> {
    /**
     * Gets an enum from it's string value.
     * @param value the string value of the enum.
     * @return the enum value derived from the string.
     */
    @Nonnull T forValue(@Nonnull String value);
}
