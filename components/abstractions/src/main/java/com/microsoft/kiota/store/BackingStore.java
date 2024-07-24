package com.microsoft.kiota.store;

import com.microsoft.kiota.TriConsumer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * Stores model information in a different location than the object properties. Implementations can provide dirty tracking capabilities, caching capabilities or integration with 3rd party stores.
 */
public interface BackingStore {
    /**
     * Gets a value from the backing store based on its key. Returns null if the value hasn't changed and "ReturnOnlyChangedValues" is true.
     * @return The value from the backing store.
     * @param key The key to lookup the backing store with.
     * @param <T> The type of the value to be retrieved.
     */
    @Nullable <T> T get(@Nonnull final String key);

    /**
     * Sets or updates the stored value for the given key.
     * Will trigger subscriptions callbacks.
     * @param key The key to store and retrieve the information.
     * @param value The value to be stored.
     * @param <T> The type of the value to be stored.
     */
    <T> void set(@Nonnull final String key, @Nullable final T value);

    /**
     * Enumerates all the values stored in the backing store. Values will be filtered if "ReturnOnlyChangedValues" is true.
     * @return The values available in the backing store.
     */
    @Nonnull Map<String, Object> enumerate();

    /**
     * Enumerates the keys for all values that changed to null.
     * @return The keys for the values that changed to null.
     */
    @Nonnull Iterable<String> enumerateKeysForValuesChangedToNull();

    /**
     * Creates a subscription to any data change happening.
     * @param callback Callback to be invoked on data changes where the first parameter is the data key, the second the previous value and the third the new value.
     * @return The subscription Id to use when removing the subscription
     */
    @Nonnull String subscribe(@Nonnull final TriConsumer<String, Object, Object> callback);

    /**
     * Creates a subscription to any data change happening, allowing to specify the subscription Id.
     * @param callback Callback to be invoked on data changes where the first parameter is the data key, the second the previous value and the third the new value.
     * @param subscriptionId The subscription Id to use.
     */
    void subscribe(
            @Nonnull final String subscriptionId,
            @Nonnull final TriConsumer<String, Object, Object> callback);

    /**
     * Removes a subscription from the store based on its subscription id.
     * @param subscriptionId The Id of the subscription to remove.
     */
    void unsubscribe(@Nonnull final String subscriptionId);

    /**
     * Removes a value from the backing store, allowing removing a previously set value. Doesn't trigger any subscription.
     * @param key The key for which the value should be removed
     */
    void remove(@Nonnull final String key);

    /**
     * Clears the data stored in the backing store. Doesn't trigger any subscription.
     */
    void clear();

    /**
     * Sets whether the initialization of the object and/or the initial deserialization has been completed to track whether objects have changed.
     * @param value value to set
     */
    void setIsInitializationCompleted(final boolean value);

    /**
     * Gets whether the initialization of the object and/or the initial deserialization has been completed to track whether objects have changed.
     * @return Whether the initialization of the object and/or the initial deserialization has been completed to track whether objects have changed.
     */
    boolean getIsInitializationCompleted();

    /**
     * Sets whether to return only values that have changed since the initialization of the object when calling the Get and Enumerate methods.
     * @param value value to set
     */
    void setReturnOnlyChangedValues(final boolean value);

    /**
     * Gets whether to return only values that have changed since the initialization of the object when calling the Get and Enumerate methods.
     * @return Whether to return only values that have changed since the initialization of the object when calling the Get and Enumerate methods.
     */
    boolean getReturnOnlyChangedValues();
}
