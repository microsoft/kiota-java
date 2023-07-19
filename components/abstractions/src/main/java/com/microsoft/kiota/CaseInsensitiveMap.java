package com.microsoft.kiota;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class CaseInsensitiveMap implements Map<String, Set<String>>{
    private final HashMap<String, HashSet<String>> internalMap = new HashMap<>();

    /**
     * Formats the string to lower case
     * @param key string to normalize to lower case
     * @return The normalized string
     */
    protected String normalizeKey(@Nonnull final String key) {
        Objects.requireNonNull(key);
        return key.toLowerCase(Locale.ROOT);
    }
    
    /** {@inheritDoc} */
    @Override
    public int size() {
        return internalMap.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(@Nonnull final Object key) {
        Objects.requireNonNull(key);
        if (key instanceof String) {
            return internalMap.containsKey(normalizeKey((String) key));
        } else {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(@Nonnull final Object value) {
        Objects.requireNonNull(value);
        return internalMap.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Set<String> get(@Nonnull final Object key) {
        Objects.requireNonNull(key);
        if (key instanceof String) {
            return internalMap.get(normalizeKey((String) key));
        } else {
            return Collections.emptySet();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Set<String> put(@Nonnull final String key, @Nonnull final Set<String> value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        return internalMap.put(normalizeKey(key), new HashSet<>(value));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Set<String> remove(@Nonnull final Object key) {
        Objects.requireNonNull(key);
        if (key instanceof String) {
            return internalMap.remove(normalizeKey((String) key));
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void putAll(@Nullable final Map<? extends String, ? extends Set<String>> m) {
        if (m == null) {
            return;
        }
        for (final Entry<? extends String, ? extends Set<String>> entry : m.entrySet()) {
            final String key = entry.getKey();
            final Set<String> value = entry.getValue();
            if (key != null && value != null) {
                internalMap.put(normalizeKey(key), new HashSet<>(value));
            }
        }
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    @Nonnull
    public Set<String> keySet() {
        return internalMap.keySet();
    }

    @Override
    @Nonnull
    public Collection<Set<String>> values() {
        return new ArrayList<>(internalMap.values());
    }

    @Override
    @Nonnull
    public Set<Entry<String, Set<String>>> entrySet() {
        final HashSet<Entry<String, Set<String>>> result = new HashSet<>();
        for (final Entry<String, HashSet<String>> entry : internalMap.entrySet()) {
            result.add(new Entry<String, Set<String>>() {
                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Set<String> getValue() {
                    return entry.getValue();
                }

                @Override
                public Set<String> setValue(Set<String> value) {
                    return entry.setValue(new HashSet<>(value));
                }
            });
        }
        return result;
    }

}