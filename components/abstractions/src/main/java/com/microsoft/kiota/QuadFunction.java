package com.microsoft.kiota;

/** Represents a callback accepting 4 values and returning a result */
public interface QuadFunction<T, U, V, W, R> {
    /**
     * Applies the callback
     * @param t the first value
     * @param u the second value
     * @param v the third value
     * @param w the fourth value
     * @return the result of the callback
     */
    R apply(T t, U u, V v, W w);
}
