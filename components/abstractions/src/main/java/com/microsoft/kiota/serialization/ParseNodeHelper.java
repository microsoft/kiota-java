package com.microsoft.kiota.serialization;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class ParseNodeHelper {
	/**
	 * Merges the given fields deserializers for an intersection type into a single collection.
	 * @param targets The collection of deserializers to merge.
	 * @return a merged collection of deserializers.
	*/
	@Nonnull
	public static Map<String, Consumer<ParseNode>> mergeDeserializersForIntersectionWrapper(@Nonnull final Parsable ...targets) {
		Objects.requireNonNull(targets, "targets cannot be null");
		if (targets.length == 0) {
			throw new IllegalArgumentException("targets cannot be empty");
		}
		final Map<String, Consumer<ParseNode>> result = targets[0].getFieldDeserializers();
		for(int i = 1; i < targets.length; i++) {
			final var targetDeserializers = targets[i].getFieldDeserializers();
			for(final var key : targetDeserializers.keySet()) {
				result.putIfAbsent(key, targetDeserializers.get(key));
			}
		}
		return result;
	}
}