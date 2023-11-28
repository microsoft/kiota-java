package com.microsoft.kiota.http;

import com.microsoft.kiota.RequestHeaders;
import com.microsoft.kiota.ResponseHeaders;

import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Objects;

/**
 * Compatibility class to bridge OkHttp Headers and Kiota Headers
 */
public class HeadersCompatibility {
    private HeadersCompatibility() {}

    /**
     * INTERNAL METHOD, DO NOT USE DIRECTLY
     * Get the response headers from the okhttp3 headers and convert them to a ResponseHeaders object
     * @param headers the okhttp3 headers
     * @return the ResponseHeaders object
     */
    @Nonnull public static ResponseHeaders getResponseHeaders(@Nonnull final okhttp3.Headers headers) {
        Objects.requireNonNull(headers);
        final ResponseHeaders responseHeaders = new ResponseHeaders();
        headers.toMultimap()
                .forEach(
                        (name, value) -> {
                            Objects.requireNonNull(name);
                            responseHeaders.put(name, new HashSet<>(value));
                        });
        return responseHeaders;
    }

    /**
     * INTERNAL METHOD, DO NOT USE DIRECTLY
     * Get the request headers from the okhttp3 headers and convert them to a RequestHeaders object
     * @param headers the okhttp3 headers
     * @return the RequestHeaders object
     */
    @Nonnull public static RequestHeaders getRequestHeaders(@Nonnull final okhttp3.Headers headers) {
        Objects.requireNonNull(headers);
        final RequestHeaders requestHeaders = new RequestHeaders();
        headers.toMultimap()
                .forEach(
                        (name, value) -> {
                            Objects.requireNonNull(name);
                            requestHeaders.put(name, new HashSet<>(value));
                        });
        return requestHeaders;
    }
}
