package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with integer value.
 */
public class UntypedInteger extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     */
    public UntypedInteger(Integer intValue) {
        value = intValue;
    }

    private final Integer value;

    /**
     * Gets the value assigned to untyped node.
     * @return The integer value of the node.
     */
    @Override
    public Integer getValue() {
        return value;
    }
}
