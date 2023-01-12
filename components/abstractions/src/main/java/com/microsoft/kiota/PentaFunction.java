package com.microsoft.kiota;

/** Represents a callback accepting 5 values and returning a result */
public interface PentaFunction<T, U, V, W, X, R> {
    /**
     * Applies the callback
     * @param t the first value
     * @param u the second value
     * @param v the third value
     * @param w the fourth value
     * @param x the fifth value
     * @return the result of the callback
     */
    R apply(T t, U u, V v, W w, X x);
}
