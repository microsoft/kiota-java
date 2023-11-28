package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestOption;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * The options to be passed to the retry middleware.
 */
public class RetryHandlerOption implements RequestOption {
    @Nonnull private final IShouldRetry mShouldRetry;

    /**
     * Default retry evaluation, always retry.
     */
    @Nonnull public static final IShouldRetry DEFAULT_SHOULD_RETRY =
            (delay, executionCount, request, response) -> true;

    private int mMaxRetries;

    /**
     * Absolute maximum number of retries
     */
    public static final int MAX_RETRIES = 10;

    /**
     * Default maximum number of retries
     */
    public static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * Delay in seconds
     */
    private long mDelay;

    /**
     * Default retry delay
     */
    public static final long DEFAULT_DELAY = 3;

    /**
     * Absolute maximum retry delay
     */
    public static final long MAX_DELAY = 180;

    /**
     * Create default instance of retry options, with default values of delay, max retries and shouldRetry callback.
     */
    public RetryHandlerOption() {
        this(DEFAULT_SHOULD_RETRY, DEFAULT_MAX_RETRIES, DEFAULT_DELAY);
    }

    /**
     * Create an instance with provided values
     * @param shouldRetry Retry callback to be called before making a retry
     * @param maxRetries Number of max retires for a request
     * @param delay Delay in seconds between retries
     */
    @SuppressWarnings("LambdaLast")
    public RetryHandlerOption(
            @Nullable final IShouldRetry shouldRetry, int maxRetries, long delay) {
        if (delay > MAX_DELAY)
            throw new IllegalArgumentException("Delay cannot exceed " + MAX_DELAY);
        if (delay < 0) throw new IllegalArgumentException("Delay cannot be negative");
        if (maxRetries > MAX_RETRIES)
            throw new IllegalArgumentException("Max retries cannot exceed " + MAX_RETRIES);
        if (maxRetries < 0) throw new IllegalArgumentException("Max retries cannot be negative");

        this.mShouldRetry = shouldRetry == null ? DEFAULT_SHOULD_RETRY : shouldRetry;
        this.mMaxRetries = maxRetries;
        this.mDelay = delay;
    }

    /**
     * Gets the callback evaluating whether a retry should be made.
     * @return should retry callback
     */
    @Nonnull public IShouldRetry shouldRetry() {
        return mShouldRetry;
    }

    /**
     * Gets the maximum number of retries.
     * @return Number of max retries
     */
    public int maxRetries() {
        return mMaxRetries;
    }

    /**
     * Gets the delay in milliseconds between retries.
     * @return Delay in milliseconds between retries
     */
    public long delay() {
        return mDelay;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) RetryHandlerOption.class;
    }
}
