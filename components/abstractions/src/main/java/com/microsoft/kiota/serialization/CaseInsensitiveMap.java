package com.microsoft.kiota.serialization;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class CaseInsensitiveMap<V> extends HashMap<String, V> {
    private static final long serialVersionUID = 1L;

    private static String normalizeKey(final String key) {
        if (key == null) {
            return null;
        }
        return key.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public V put(final String key, final V value) {
        return super.put(normalizeKey(key), value);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends V> m) {
        Objects.requireNonNull(m);
        for (final Map.Entry<? extends String, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V get(final Object key) {
        return super.get(key instanceof String ? normalizeKey((String) key) : key);
    }

    @Override
    public boolean containsKey(final Object key) {
        return super.containsKey(key instanceof String ? normalizeKey((String) key) : key);
    }

    @Override
    public V remove(final Object key) {
        return super.remove(key instanceof String ? normalizeKey((String) key) : key);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return super.getOrDefault(
                key instanceof String ? normalizeKey((String) key) : key, defaultValue);
    }

    @Override
    public V putIfAbsent(final String key, final V value) {
        return super.putIfAbsent(normalizeKey(key), value);
    }

    @Override
    public V computeIfAbsent(
            final String key, final Function<? super String, ? extends V> mappingFunction) {
        final String normalized = normalizeKey(key);
        return super.computeIfAbsent(normalized, mappingFunction);
    }
}
