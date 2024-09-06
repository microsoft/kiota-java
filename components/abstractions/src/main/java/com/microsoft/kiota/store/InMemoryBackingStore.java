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
    private final String parentSubscriptionId = "x-parent-subscriptionId";

    public void setIsInitializationCompleted(final boolean value) {
        this.isInitializationCompleted = value;

        // Propagate status to current store & store's nested items
        // Handles case where there are already existing items in the store
        // e.g. generated models initialize additional data & OData type in the constructors
        markDirtyStatusOfAllProperties(!value);
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            final Pair<Boolean, Object> wrapper = entry.getValue();
            if (wrapper.getValue1() instanceof BackedModel) {
                BackedModel backedModel = (BackedModel) wrapper.getValue1();
                backedModel
                        .getBackingStore()
                        .setIsInitializationCompleted(value); // propagate initialization
            }
            if (wrapper.getValue1() instanceof Pair) {
                Pair<Object, Integer> pair = (Pair<Object, Integer>) wrapper.getValue1();

                if (pair.getValue0() instanceof Collection || pair.getValue0() instanceof Map) {
                    // Update pair size without changing property's dirty tracking
                    Integer previousSize = (Integer) pair.getValue1();
                    Integer currentSize;
                    Collection<Object> items;

                    if (pair.getValue0() instanceof Collection) {
                        currentSize = ((Collection<?>) pair.getValue0()).size();
                        items = (Collection<Object>) pair.getValue0();
                    } else {
                        currentSize = ((Map<?, ?>) pair.getValue0()).size();
                        items = ((Map<?, Object>) pair.getValue0()).values();
                    }
                    if (previousSize != currentSize) {
                        Pair<Object, Integer> updatedValue = pair.setValue1(currentSize);
                        entry.setValue(new Pair<>(entry.getValue().getValue0(), updatedValue));
                    }

                    // propagate initialization
                    for (Object item : items) {
                        if (item instanceof BackedModel) {
                            BackingStore store = ((BackedModel) item).getBackingStore();
                            store.setIsInitializationCompleted(value);
                        }
                    }
                }
            }

        }
    }

    public boolean getIsInitializationCompleted() {
        return this.isInitializationCompleted;
    }

    public void setReturnOnlyChangedValues(final boolean value) {
        this.returnOnlyChangedValues = value;

        if (value) {
            if (isNestedBackedModel()) {
                markDirtyStatusOfAllProperties(true);
            }
        }
    }

    public boolean getReturnOnlyChangedValues() {
        return this.returnOnlyChangedValues;
    }

    public void clear() {
        this.store.clear();
    }

    /**
     * Enumerates the properties in the store
     * Checks collection values for consistency and updates the store if necessary
     * If the property has changed, the value is returned.
     * If parent property has changed, all values of child backing stores are returned.
     */
    @Nonnull public Map<String, Object> enumerate() {
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            // Get() checks consistency of collection types and returns the value if it has changed
            // & returnOnlyChangedValues is true, else null
            Object value = get(entry.getKey());
            if (!getReturnOnlyChangedValues()) {
                result.put(entry.getKey(), value);
                continue;
            }

            // get() may return null to mean that the value has not changed
            // check if value has changed but changed to null
            if (value != null || entry.getValue().getValue0()) {
                // value exists and has changed
                result.put(entry.getKey(), value);
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
            if (wrapper.getValue1() instanceof Pair) {
                Pair<?, ?> collectionTuple = (Pair<?, ?>) wrapper.getValue1();
                return collectionTuple.getValue0();
            }
            return wrapper.getValue1();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable public <T> T get(@Nonnull final String key) {
        Objects.requireNonNull(key);
        final Pair<Boolean, Object> wrapper = this.store.get(key);
        Object value = this.getValueFromWrapper(key, wrapper);

        if (!getReturnOnlyChangedValues()) {
            if (value == null) {
                return null;
            }
            try {
                return (T) value;
            } catch (ClassCastException ex) {
                return null;
            }
        }

        // Double check collections are consistent in nested BackedModels
        if (value instanceof BackedModel) {
            ensureCollectionPropertiesAreConsistent(((BackedModel) value).getBackingStore());
        }
        if (value instanceof Collection || value instanceof Map) {
            // Check that current value is consistent if value is not dirty
            boolean isPropertyDirty = wrapper.getValue0();
            if (!isPropertyDirty) {
                // set() checks size and updates dirty tracking status
                set(key, value);
                value = this.getValueFromWrapper(key, this.store.get(key));
            }

            // Double check collections are consistent in nested BackedModels
            Collection<Object> items;
            if (value instanceof Collection) {
                items = ((Collection<Object>) value);
            } else {
                items = ((Map<?, Object>) value).values();
            }
            for (Object item : items) {
                if (item instanceof BackedModel) {
                    BackingStore store = ((BackedModel) item).getBackingStore();
                    ensureCollectionPropertiesAreConsistent(store);
                }
            }
        }

        // Fetch latest value if it has been updated
        Pair<Boolean, Object> latestWrapper = this.store.get(key);
        Object latestValue = this.getValueFromWrapper(key, wrapper);

        // Return null if value hasn't changed.
        if (Boolean.FALSE.equals(latestWrapper.getValue0())) {
            return null;
        }

        try {
            return (T) latestValue;
        } catch (ClassCastException ex) {
            return null;
        }
    }

    /**
     * Used to add a property key and value to the store
     * Used to mark an existing property as dirty if the key already exists. Propagates this state up to the parent BackedModels
     * by calling subscriptions. Propagates this state down to nested BackedModels and nested collections of BackedModels
     */
    public <T> void set(@Nonnull final String key, @Nullable final T value) {
        Objects.requireNonNull(key);

        Pair<Boolean, Object> valueToAdd = new Pair<>(this.isInitializationCompleted, value);
        if (value instanceof Collection) {
            valueToAdd = valueToAdd.setValue1(new Pair<>(value, ((Collection<?>) value).size()));
        } else if (value instanceof Map) {
            valueToAdd = valueToAdd.setValue1(new Pair<>(value, ((Map<?, ?>) value).size()));
        }

        if (this.store.containsKey(key)) {
            // Check if the size of the collection has changed and mark property as dirty
            if (value instanceof Collection || value instanceof Map) {
                if (this.store.get(key).getValue1() instanceof Pair) {
                    Pair<T, Integer> valuePair = (Pair<T, Integer>) this.store.get(key).getValue1();
                    Integer previousSize = (Integer) valuePair.getValue1();
                    Integer currentSize = ((Pair<?, Integer>) valueToAdd.getValue1()).getValue1();
                    if (previousSize != currentSize || valuePair.getValue0() != value) {
                        // Mark property as dirty
                        valueToAdd = valueToAdd.setValue0(true);
                    } else {
                        valueToAdd = valueToAdd.setValue0(false);
                        // Update store with new value
                        this.store.put(key, valueToAdd);
                        setupNestedSubscriptions(key, value);
                        // Don't trigger subscriptions if the size hasn't changed
                        // Parent properties will be wrongly marked as dirty by
                        // isInitializationCompleted
                        return;
                    }
                }
            }
        }

        // Update store with new value
        final Pair<Boolean, Object> oldValue = this.store.put(key, valueToAdd);
        setupNestedSubscriptions(key, value);
        triggerSubscriptions(key, oldValue, value);
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

    private <T> void setupNestedSubscriptions(final String key, final T value) {
        // Propagate subscriptions to nested objects
        if (value instanceof Collection || value instanceof Map) {
            Collection<Object> items;
            if (value instanceof Collection) {
                items = ((Collection<Object>) value);
            } else {
                items = ((Map<?, Object>) value).values();
            }
            for (Object item : items) {
                if (item instanceof BackedModel) {
                    BackingStore store = ((BackedModel) item).getBackingStore();
                    setupNestedSubscriptions(store, key, value);
                }
            }
        }
        if (value instanceof BackedModel) {
            setupNestedSubscriptions(((BackedModel) value).getBackingStore(), key, value);
        }
    }

    /**
     * Traverses hierarchy of BackedModels registering a subscription
     *
     * @param backingStore
     * @param key
     * @param value
     */
    private void setupNestedSubscriptions(
            final BackingStore backingStore, final String key, final Object value) {

        setupDirtyTrackingSubscription(backingStore, key, value);

        boolean previousReturnOnlyChangedValues = backingStore.getReturnOnlyChangedValues();
        // ensure all values are returned
        backingStore.setReturnOnlyChangedValues(false);

        for (final Map.Entry<String, Object> entry : backingStore.enumerate().entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof BackedModel) {
                    BackingStore store = ((BackedModel) entry.getValue()).getBackingStore();
                    setupNestedSubscriptions(store, key, value);
                }
                if (entry.getValue() instanceof Collection || entry.getValue() instanceof Map) {
                    Object[] items;
                    if (entry.getValue() instanceof Collection) {
                        items = ((Collection<?>) entry.getValue()).toArray();
                    } else {
                        items = ((Map<?, Object>) entry.getValue()).values().toArray();
                    }
                    for (Object item : items) {
                        if (item instanceof BackedModel) {
                            BackingStore store = ((BackedModel) item).getBackingStore();
                            setupNestedSubscriptions(store, key, value);
                        }
                    }
                }
            }
        }
        backingStore.setReturnOnlyChangedValues(previousReturnOnlyChangedValues);
    }

    private void setupDirtyTrackingSubscription(
            final BackingStore backingStore, final String key, final Object value) {
        if (backingStore != null) {
            backingStore.subscribe(
                    // use property name(key) as subscriptionId to prevent excess
                    // subscription creation in the event this is called again
                    parentSubscriptionId, (keyString, oldObject, newObject) -> set(key, value));
        }
    }

    private void ensureCollectionPropertiesAreConsistent(BackingStore backingStore) {
        boolean previousReturnOnlyChangedValues = backingStore.getReturnOnlyChangedValues();
        // ensure all values are returned
        backingStore.setReturnOnlyChangedValues(false);

        for (final Map.Entry<String, Object> entry : backingStore.enumerate().entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof BackedModel) {
                    BackingStore store = ((BackedModel) entry.getValue()).getBackingStore();
                    ensureCollectionPropertiesAreConsistent(store);
                }
                if (entry.getValue() instanceof Collection || entry.getValue() instanceof Map) {
                    // call set() again to mark property as dirty if the collection has changed
                    // set() also triggers necessary subscriptions
                    backingStore.set(entry.getKey(), entry.getValue());

                    Object[] items;
                    if (entry.getValue() instanceof Collection) {
                        items = ((Collection<?>) entry.getValue()).toArray();
                    } else {
                        items = ((Map<?, Object>) entry.getValue()).values().toArray();
                    }
                    for (Object item : items) {
                        if (item instanceof BackedModel) {
                            BackingStore store = ((BackedModel) item).getBackingStore();
                            ensureCollectionPropertiesAreConsistent(store);
                        }
                    }
                }
            }
        }

        backingStore.setReturnOnlyChangedValues(previousReturnOnlyChangedValues);
    }

    private void triggerSubscriptions(
            final String key, final Pair<Boolean, Object> oldValue, final Object value) {
        // Trigger subscriptions to parent items if necessary
        for (final TriConsumer<String, Object, Object> callback : this.subscriptionStore.values()) {
            if (oldValue != null) {
                callback.accept(key, oldValue.getValue1(), value);
            } else {
                callback.accept(key, null, value);
            }
        }
    }

    private boolean isNestedBackedModel() {
        return this.subscriptionStore.containsKey(parentSubscriptionId);
    }

    private void markDirtyStatusOfAllProperties(boolean dirty) {
        for (final Map.Entry<String, Pair<Boolean, Object>> entry : this.store.entrySet()) {
            Pair<Boolean, Object> value = entry.getValue();
            final Pair<Boolean, Object> updatedValue = value.setValue0(dirty);
            entry.setValue(updatedValue);
        }
    }
}
