package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with integer value.
 */
public class UntypedInteger extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     * @param intValue The integer to create the node with.
     */
    public UntypedInteger(@Nonnull Integer intValue) {
        value = intValue;
    }

    private final Integer value;

    /**
     * Gets the value assigned to untyped node.
     * @return The integer value of the node.
     */
    @Override
    @Nonnull public Integer getValue() {
        return value;
    }
}
