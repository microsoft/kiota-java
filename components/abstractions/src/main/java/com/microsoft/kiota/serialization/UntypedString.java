package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with string value.
 */
public class UntypedString extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     */
    public UntypedString(String stringValue) {
        value = stringValue;
    }

    private final String value;

    /**
     * Gets the value assigned to untyped node.
     * @return The string value of the node.
     */
    @Override
    public String getValue() {
        return value;
    }
}
