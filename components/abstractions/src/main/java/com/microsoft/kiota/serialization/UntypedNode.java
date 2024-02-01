package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for untyped node.
 */
public class UntypedNode implements Parsable {

    /**
     * The deserialization information for the current model.
     * @return The map of serializer methods for this object.
     */
    @Nonnull @Override
    public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        return new HashMap<>();
    }

    /**
     * Serializes the current object
     */
    @Override
    public void serialize(@Nonnull SerializationWriter writer) {}

    /**
     * Gets the value assigned to untyped node.
     * @return The value assigned to untyped node.
     */
    public Object getValue() {
        return null;
    }

    /**
     * Creates a new instance of the appropriate class based on discriminator value.
     * @return A new UntypedNode instance.
     */
    public static UntypedNode createFromDiscriminatorValue(@Nonnull final ParseNode parseNode) {
        return new UntypedNode();
    }
}
