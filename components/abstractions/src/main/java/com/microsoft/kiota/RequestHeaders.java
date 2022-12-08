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

/**
 * A class representing the headers of a request.
 */
public class RequestHeaders implements Map<String, Set<String>> {
    private final HashMap<String, HashSet<String>> headers = new HashMap<>();
    /**
     * Adds a header to the current request.
     * @param key the key of the header to add.
     * @param value the value of the header to add.
     */
    public void add(@Nonnull final String key, @Nonnull final String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        final String normalizedKey = normalizeKey(key);
        if(headers.containsKey(normalizedKey)) {
            final HashSet<String> values = headers.get(normalizedKey);
            values.add(value);
        } else {
			final HashSet<String> values = new HashSet<>(1);
			values.add(value);
            headers.put(normalizedKey, values);
        }
    }
    private String normalizeKey(@Nonnull final String key) {
        Objects.requireNonNull(key);
        return key.toLowerCase(Locale.ROOT);
    }
    @Override
    public int size() {
        return headers.size();
    }
    @Override
    public boolean isEmpty() {
        return headers.isEmpty();
    }
    @Override
    public boolean containsKey(@Nonnull final Object key) {
		Objects.requireNonNull(key);
        if (key instanceof String) {
            return headers.containsKey(normalizeKey((String)key));
        } else {
            return false;
        }
    }
    @Override
    public boolean containsValue(@Nonnull final Object value) {
		Objects.requireNonNull(value);
        return headers.containsValue(value);
    }
    @Override
	@Nonnull
    public Set<String> get(@Nonnull final Object key) {
		Objects.requireNonNull(key);
        if (key instanceof String) {
            return headers.get(normalizeKey((String)key));
        } else {
            return Collections.emptySet();
        }
    }
    @Override
	@Nonnull
    public Set<String> put(@Nonnull final String key, @Nonnull final Set<String> value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        return headers.put(normalizeKey(key), new HashSet<>(value));
    }
    @Override
	@Nonnull
    public Set<String> remove(@Nonnull final Object key) {
        if (key instanceof String) {
            return headers.remove(normalizeKey((String)key));
        } else {
            return Collections.emptySet();
        }
    }
	/**
	 * Removes a value from a header
	 * @param key the key of the header to remove the value from
	 * @param value the value to remove
	 * @return true if the value was removed, false otherwise
	 */
    public boolean remove(@Nonnull final String key, @Nonnull final String value) { 
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		final String normalizedKey = normalizeKey(key);
		if(headers.containsKey(normalizedKey)) {
			final HashSet<String> values = headers.get(normalizedKey);
			if(values.contains(value)) {
				values.remove(value);
				return true;
			}
			if (values.isEmpty()) {
				headers.remove(normalizedKey);
			}
		}
		return false;
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
                headers.put(normalizeKey(key), new HashSet<>(value));
            }    
        }
    }
    @Override
    public void clear() {
        headers.clear();
    }
    @Override
	@Nonnull
    public Set<String> keySet() {
        return headers.keySet();
    }
    @Override
	@Nonnull
    public Collection<Set<String>> values() {
        return new ArrayList<>(headers.values());
    }
    @Override
	@Nonnull
    public Set<Entry<String, Set<String>>> entrySet() {
        final HashSet<Entry<String, Set<String>>> result = new HashSet<>();
        for (final Entry<String, HashSet<String>> entry : headers.entrySet()) {
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