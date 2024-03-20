package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

/**
 * Represents an untyped node with a collection of other untyped nodes.
 */
public class UntypedArray extends UntypedNode {
    /**
     * The constructor for the UntypedArray
     * @param collection Collection to initialize with.
     */
    public UntypedArray(@Nonnull Iterable<UntypedNode> collection) {
        value = collection;
    }

    private final Iterable<UntypedNode> value;

    /**
     * Gets the value assigned to untyped node.
     * @return The string value of the node.
     */
    @Override
    @Nonnull public Iterable<UntypedNode> getValue() {
        return value;
    }
}
