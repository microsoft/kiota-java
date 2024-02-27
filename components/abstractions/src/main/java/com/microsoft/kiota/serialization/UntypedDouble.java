package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with double value.
 */
public class UntypedDouble extends UntypedNode {
    /**
     * The constructor for the UntypedDouble
     * @param doubleValue The Double to create the node with.
     */
    public UntypedDouble(@Nonnull Double doubleValue) {
        value = doubleValue;
    }

    private final Double value;

    /**
     * Gets the value assigned to untyped node.
     * @return The double value of the node.
     */
    @Override
    @Nonnull public Double getValue() {
        return value;
    }
}
