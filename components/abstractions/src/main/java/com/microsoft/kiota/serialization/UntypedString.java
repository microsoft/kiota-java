package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with string value.
 */
public class UntypedString extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     * @param stringValue The string to create the node with.
     */
    public UntypedString(@Nonnull String stringValue) {
        value = stringValue;
    }

    private final String value;

    /**
     * Gets the value assigned to untyped node.
     * @return The string value of the node.
     */
    @Override
    @Nonnull public String getValue() {
        return value;
    }
}
