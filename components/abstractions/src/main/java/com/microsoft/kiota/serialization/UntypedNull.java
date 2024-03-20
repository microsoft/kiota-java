package com.microsoft.kiota.serialization;

import jakarta.annotation.Nullable;

/**
 * Represents an untyped node with null value.
 */
public class UntypedNull extends UntypedNode {
    /**
     * The default constructor for the UntypedNull
     */
    public UntypedNull() {
        // empty constructor
    }

    /**
     * Gets the value assigned to untyped node.
     * @return null value.
     */
    @Override
    @Nullable public Object getValue() {
        return null;
    }
}
