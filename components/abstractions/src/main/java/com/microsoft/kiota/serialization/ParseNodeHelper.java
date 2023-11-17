package com.microsoft.kiota.serialization;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

/** Utility methods to reduce the amount of code being generated. */
public class ParseNodeHelper {
    /** Default constructor */
    private ParseNodeHelper() {}

    /**
     * Merges the given fields deserializers for an intersection type into a single collection.
     * @param targets The collection of deserializers to merge.
     * @return a merged collection of deserializers.
     */
    @Nonnull public static Map<String, Consumer<ParseNode>> mergeDeserializersForIntersectionWrapper(
            @Nonnull final Parsable... targets) {
        Objects.requireNonNull(targets, "targets cannot be null");
        if (targets.length == 0) {
            throw new IllegalArgumentException("targets cannot be empty");
        }
        final Map<String, Consumer<ParseNode>> result = targets[0].getFieldDeserializers();
        for (int i = 1; i < targets.length; i++) {
            final Map<String, Consumer<ParseNode>> targetDeserializers =
                    targets[i].getFieldDeserializers();
            for (final Entry<String, Consumer<ParseNode>> entry : targetDeserializers.entrySet()) {
                result.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
