package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with double value.
 */
public class UntypedDouble extends UntypedNode {
    /**
     * The constructor for the UntypedDouble
     * @param doubleValue The Double to create the node with.
     */
    public UntypedDouble(Double doubleValue) {
        value = doubleValue;
    }

    private final Double value;

    /**
     * Gets the value assigned to untyped node.
     * @return The double value of the node.
     */
    @Override
    public Double getValue() {
        return value;
    }
}
