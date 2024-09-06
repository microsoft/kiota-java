package com.microsoft.kiota.store;

import com.microsoft.kiota.TriConsumer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** In-memory implementation of the backing store. Allows for dirty tracking of changes. */
public class InMemoryBackingStore implements BackingStore {
    /** Creates a new instance of the backing store. */
    public InMemoryBackingStore() {
        // default constructor
    }

    private static class Pair<A, B> {
        private final A value0;
        private final B value1;

        public Pair(A value0, B value1) {
            this.value0 = value0;
            this.value1 = value1;
        }

        public A getValue0() {
            return value0;
        }

        public Pair<A, B> setValue0(A value0) {
            return new Pair<>(value0, value1);
        }

        public B getValue1() {
            return value1;
        }

        public Pair<A, B> setValue1(B value1) {
            return new Pair<>(value0, value1);
        }
    }

    private boolean isInitializationCompleted = true;
    private boolean returnOnlyChangedValues;
    private final Map<String, Pair<Boolean, Object>> store = new HashMap<>();
    private final Map<String, TriConsumer<String, Object, Object>> subscriptionStore =
            new HashMap<>();

    public void setIsInitializationCompleted(final boolean value) {
        this.isInitializationCompleted = value;
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            final Pair<Boolean, Object> wrapper = entry.getValue();
            if (wrapper.getValue1() instanceof BackedModel) {
                BackedModel backedModel = (BackedModel) wrapper.getValue1();
                backedModel
                        .getBackingStore()
                        .setIsInitializationCompleted(value); // propagate initialization
            } else {
                // setIsInitializationCompleted() called above already checks for collection
                // consistency for BackedModels
                ensureCollectionPropertyIsConsistent(
                        entry.getKey(), this.store.get(entry.getKey()).getValue1());
            }
            final Pair<Boolean, Object> updatedValue = wrapper.setValue0(!value);
            entry.setValue(updatedValue);
        }
    }

    public boolean getIsInitializationCompleted() {
        return this.isInitializationCompleted;
    }

    public void setReturnOnlyChangedValues(final boolean value) {
        this.returnOnlyChangedValues = value;
    }

    public boolean getReturnOnlyChangedValues() {
        return this.returnOnlyChangedValues;
    }

    public void clear() {
        this.store.clear();
    }

    @Nonnull public Map<String, Object> enumerate() {
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            final Pair<Boolean, Object> wrapper = entry.getValue();
            final Object value = this.get(entry.getKey());

            if (value != null) {
                result.put(entry.getKey(), value);
            } else if (Boolean.TRUE.equals(wrapper.getValue0())) {
                result.put(entry.getKey(), null);
            }
        }
        return result;
    }

    @Nonnull public Iterable<String> enumerateKeysForValuesChangedToNull() {
        final List<String> result = new ArrayList<>();
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            final Pair<Boolean, Object> wrapper = entry.getValue();
            final Object value = wrapper.getValue1();
            if (value == null && wrapper.getValue0()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Object getValueFromWrapper(final String entryKey, final Pair<Boolean, Object> wrapper) {
        if (wrapper != null) {
            final Boolean hasChanged = wrapper.getValue0();
            if (!this.returnOnlyChangedValues || Boolean.TRUE.equals(hasChanged)) {
                if (Boolean.FALSE.equals(
                        hasChanged)) { // no need property has already been flagged.
                    ensureCollectionPropertyIsConsistent(entryKey, wrapper.getValue1());
                }
                if (wrapper.getValue1() instanceof Pair) {
                    Pair<?, ?> collectionTuple = (Pair<?, ?>) wrapper.getValue1();
                    return collectionTuple.getValue0();
                }
                return wrapper.getValue1();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable public <T> T get(@Nonnull final String key) {
        Objects.requireNonNull(key);
        final Pair<Boolean, Object> wrapper = this.store.get(key);
        final Object value = this.getValueFromWrapper(key, wrapper);
        try {
            return (T) value;
        } catch (ClassCastException ex) {
            return null;
        }
    }

    public <T> void set(@Nonnull final String key, @Nullable final T value) {
        Objects.requireNonNull(key);
        Pair<Boolean, Object> valueToAdd = new Pair<>(this.isInitializationCompleted, value);
        if (value instanceof Collection) {
            valueToAdd = valueToAdd.setValue1(new Pair<>(value, ((Collection<?>) value).size()));
            final Collection<Object> items = (Collection<Object>) value;
            setupNestedSubscriptions(items, key, value);
        } else if (value instanceof Map) {
            valueToAdd = valueToAdd.setValue1(new Pair<>(value, ((Map<?, ?>) value).size()));
            final Map<?, Object> items = (Map<?, Object>) value;
            setupNestedSubscriptions(items.values(), key, value);
        } else if (value instanceof BackedModel) {
            final BackedModel backedModel = (BackedModel) value;
            backedModel
                    .getBackingStore()
                    .subscribe(
                            key,
                            (keyString, oldObject, newObject) -> {
                                backedModel
                                        .getBackingStore()
                                        .setIsInitializationCompleted(
                                                false); // All its properties are dirty as the model
                                // has been touched.
                                set(key, value);
                            }); // use property name(key) as subscriptionId to prevent excess
            // subscription creation in the event this is called again
        }

        final Pair<Boolean, Object> oldValue = this.store.put(key, valueToAdd);
        for (final TriConsumer<String, Object, Object> callback : this.subscriptionStore.values()) {
            if (oldValue != null) {
                callback.accept(key, oldValue.getValue1(), value);
            } else {
                callback.accept(key, null, value);
            }
        }
    }

    public void unsubscribe(@Nonnull final String subscriptionId) {
        Objects.requireNonNull(subscriptionId);
        this.subscriptionStore.remove(subscriptionId);
    }

    @Nonnull public String subscribe(@Nonnull final TriConsumer<String, Object, Object> callback) {
        final String subscriptionId = UUID.randomUUID().toString();
        subscribe(subscriptionId, callback);
        return subscriptionId;
    }

    public void subscribe(
            @Nonnull final String subscriptionId,
            @Nonnull final TriConsumer<String, Object, Object> callback) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(subscriptionId);
        this.subscriptionStore.put(subscriptionId, callback);
    }

    private void setupNestedSubscriptions(
            final Collection<Object> items, final String key, final Object value) {
        for (final Object item : items) {
            if (item instanceof BackedModel) {
                final BackedModel backedModel = (BackedModel) item;
                backedModel.getBackingStore().setIsInitializationCompleted(false);
                backedModel
                        .getBackingStore()
                        .subscribe(key, (keyString, oldObject, newObject) -> set(key, value));
            }
        }
    }

    private void ensureCollectionPropertyIsConsistent(final String key, final Object storeItem) {
        if (storeItem instanceof Pair) { // check if we put in a collection annotated with the size
            final Pair<?, Integer> collectionTuple = (Pair<?, Integer>) storeItem;
            Object[] items;
            if (collectionTuple.getValue0() instanceof Collection) {
                items = ((Collection<Object>) collectionTuple.getValue0()).toArray();
            } else { // it is a map
                items = ((Map<?, Object>) collectionTuple.getValue0()).values().toArray();
            }

            for (final Object item : items) {
                touchNestedProperties(item); // call get on nested properties
            }

            if (collectionTuple.getValue1()
                    != items.length) { // and the size has changed since we last updated
                set(
                        key,
                        collectionTuple.getValue0()); // ensure the store is notified the collection
                // property is "dirty"
            }
        }
        touchNestedProperties(storeItem); // call get on nested properties
    }

    private void touchNestedProperties(final Object nestedObject) {
        if (nestedObject instanceof BackedModel) {
            // Call Get<>() on nested properties so that this method may be called recursively to
            // ensure collections are consistent
            final BackedModel backedModel = (BackedModel) nestedObject;
            // enumerate() calls get<>() on all properties
            backedModel.getBackingStore().enumerate();
        }
    }
}
