package com.microsoft.kiota.serialization;

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
    public Object getValue() {
        return null;
    }
}
