package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with Long value.
 */
public class UntypedLong extends UntypedNode {
    /**
     * The constructor for the UntypedLong
     */
    public UntypedLong(Long longValue) {
        value = longValue;
    }

    private final Long value;

    /**
     * Gets the value assigned to untyped node.
     * @return The long value of the node.
     */
    @Override
    public Long getValue() {
        return value;
    }
}
