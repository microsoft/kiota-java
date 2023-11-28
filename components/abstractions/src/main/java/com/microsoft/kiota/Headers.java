package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class representing the headers of a request or a response.
 */
public abstract class Headers extends CaseInsensitiveMap {
    /** Default constructor */
    protected Headers() {
        super();
    }

    /** Copy constructor
     * @param headers The headers to initialize with.
     */
    protected Headers(@Nonnull Headers headers) {
        super();
        Objects.requireNonNull(headers);
        putAll(headers);
    }

    /**
     * Adds a header to the current request.
     *
     * @param key   the key of the header to add.
     * @param value the value of the header to add.
     */
    public void add(@Nonnull final String key, @Nonnull final String value) {
        addImpl(key, value, true);
    }

    /**
     * Adds a header to the current request if it was not already set
     *
     * @param key   the key of the header to add.
     * @param value the value of the header to add.
     * @return if the value have been added
     */
    public boolean tryAdd(@Nonnull final String key, @Nonnull final String value) {
        return addImpl(key, value, false);
    }

    private boolean addImpl(
            @Nonnull final String key, @Nonnull final String value, boolean appendIfPresent) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final String normalizedKey = normalizeKey(key);
        if (this.containsKey(normalizedKey)) {
            if (appendIfPresent) {
                final Set<String> values = this.get(normalizedKey);
                values.add(value);
                return true;
            }
        } else {
            final Set<String> values = new HashSet<>(1);
            values.add(value);
            this.put(normalizedKey, values);
            return true;
        }
        return false;
    }

    /**
     * Removes a value from a header
     *
     * @param key   the key of the header to remove the value from
     * @param value the value to remove
     * @return true if the value was removed, false otherwise
     */
    public boolean remove(@Nonnull final String key, @Nonnull final String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final String normalizedKey = normalizeKey(key);
        if (this.containsKey(normalizedKey)) {
            final Set<String> values = this.get(normalizedKey);
            if (values.contains(value)) {
                values.remove(value);
                if (values.isEmpty()) {
                    this.remove(normalizedKey);
                }
                return true;
            }
        }
        return false;
    }
}
