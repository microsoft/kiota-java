package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Defines the factory for creating parsable objects.
 * @param <T> The type of the parsable object.
 */
@FunctionalInterface
public interface ParsableFactory<T extends Parsable> {
    /**
     * Create a new parsable object from the given serialized data.
     * @param parseNode The node to parse use to get the discriminator value from the payload.
     * @return The parsable object.
     */
    T create(@Nonnull final ParseNode parseNode);
}
