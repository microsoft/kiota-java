package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with Float value.
 */
public class UntypedFloat extends UntypedNode {
    /**
     * The constructor for the UntypedFloat
     * @param floatValue The float value to create the node with.
     */
    public UntypedFloat(@Nonnull Float floatValue) {
        value = floatValue;
    }

    private final Float value;

    /**
     * Gets the value assigned to untyped node.
     * @return The float value of the node.
     */
    @Override
    @Nonnull public Float getValue() {
        return value;
    }
}
