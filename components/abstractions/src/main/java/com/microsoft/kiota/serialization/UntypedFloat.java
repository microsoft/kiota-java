package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with Float value.
 */
public class UntypedFloat extends UntypedNode {
    /**
     * The constructor for the UntypedFloat
     */
    public UntypedFloat(Float floatValue) {
        value = floatValue;
    }

    private final Float value;

    /**
     * Gets the value assigned to untyped node.
     * @return The float value of the node.
     */
    @Override
    public Float getValue() {
        return value;
    }
}
