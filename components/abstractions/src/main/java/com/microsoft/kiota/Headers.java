package com.microsoft.kiota;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

class Headers extends CaseInsensitiveMap {
    /** Default constructor */
    public Headers() {
        super();
    }

    /** Copy constructor */
    public Headers(@Nonnull Headers headers) {
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
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final String normalizedKey = normalizeKey(key);
        if (this.containsKey(normalizedKey)) {
            final Set<String> values = this.get(normalizedKey);
            values.add(value);
        } else {
            final Set<String> values = new HashSet<>(1);
            values.add(value);
            this.put(normalizedKey, values);
        }
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
