package com.microsoft.kiota.serialization;

/**
 * Represents an untyped node with a collection of other untyped nodes.
 */
public class UntypedArray extends UntypedNode {
    /**
     * The constructor for the UntypedArray
     */
    public UntypedArray(Iterable<UntypedNode> collection) {
        value = collection;
    }

    private final Iterable<UntypedNode> value;

    /**
     * Gets the value assigned to untyped node.
     * @return The string value of the node.
     */
    @Override
    public Iterable<UntypedNode> getValue() {
        return value;
    }
}
