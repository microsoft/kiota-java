package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with boolean value.
 */
public class UntypedBoolean extends UntypedNode {
    /**
     * The constructor for the UntypedBoolean
     * @param boolValue Boolean to create node with.
     */
    public UntypedBoolean(@Nonnull Boolean boolValue) {
        value = boolValue;
    }

    private final Boolean value;

    /**
     * Gets the value assigned to untyped node.
     * @return The bool value of the node.
     */
    @Override
    @Nonnull public Boolean getValue() {
        return value;
    }
}
