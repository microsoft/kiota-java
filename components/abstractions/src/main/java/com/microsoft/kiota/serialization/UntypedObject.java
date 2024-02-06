package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an untyped node with object value.
 */
public class UntypedObject extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     * @param propertiesMap The Map to create the node with
     */
    public UntypedObject(@Nonnull Map<String, UntypedNode> propertiesMap) {
        properties = new HashMap<>(propertiesMap);
    }

    private final Map<String, UntypedNode> properties;

    /**
     * Gets the value assigned to untyped node.
     * @return The Map of property keys and their values.
     */
    @Override
    @Nonnull public Map<String, UntypedNode> getValue() {
        return new HashMap<>(properties);
    }
}
