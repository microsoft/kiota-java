package com.microsoft.kiota.serialization;

import java.util.Map;

/**
 * Represents an untyped node with object value.
 */
public class UntypedObject extends UntypedNode {
    /**
     * The constructor for the UntypedObject
     */
    public UntypedObject(Map<String, UntypedNode> propertiesMap) {
        properties = propertiesMap;
    }

    private final Map<String, UntypedNode> properties;

    /**
     * Gets the value assigned to untyped node.
     * @return The Map of property keys and their values.
     */
    @Override
    public Map<String, UntypedNode> getValue() {
        return properties;
    }
}
