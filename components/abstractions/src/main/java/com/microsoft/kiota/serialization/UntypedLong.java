package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with Long value.
 */
public class UntypedLong extends UntypedNode {
    /**
     * The constructor for the UntypedLong
     * @param longValue The long value to create the node with.
     */
    public UntypedLong(@Nonnull Long longValue) {
        value = longValue;
    }

    private final Long value;

    /**
     * Gets the value assigned to untyped node.
     * @return The long value of the node.
     */
    @Override
    @Nonnull public Long getValue() {
        return value;
    }
}
