package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestOption;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.Request;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Options to be passed to the redirect middleware.
 */
public class RedirectHandlerOption implements RequestOption {
    private int maxRedirects;

    /**
     * The default maximum number of redirects to follow
     */
    public static final int DEFAULT_MAX_REDIRECTS = 5;

    /**
     * The absolute maximum number of redirects that can be followed
     */
    public static final int MAX_REDIRECTS = 20;

    @Nonnull private final IShouldRedirect shouldRedirect;

    /**
     * Default redirect evaluation, always follow redirect information.
     */
    @Nonnull public static final IShouldRedirect DEFAULT_SHOULD_REDIRECT = response -> true;

    @Nonnull private final IScrubSensitiveHeaders scrubSensitiveHeaders;

    /**
     * Functional interface for scrubbing sensitive headers during redirects.
     */
    @FunctionalInterface
    public interface IScrubSensitiveHeaders {
        /**
         * Scrubs sensitive headers from the request before following a redirect.
         * @param requestBuilder The request builder to modify
         * @param originalUrl The original request URL
         * @param newUrl The new redirect URL
         * @param proxyResolver A function that returns the proxy for a given destination, or null if no proxy applies
         */
        void scrubHeaders(
                @Nonnull Request.Builder requestBuilder,
                @Nonnull HttpUrl originalUrl,
                @Nonnull HttpUrl newUrl,
                @Nullable Function<HttpUrl, Proxy> proxyResolver);
    }

    /**
     * The default implementation for scrubbing sensitive headers during redirects.
     * This method removes Authorization and Cookie headers when the host or scheme changes,
     * and removes Proxy-Authorization headers when no proxy is configured or the proxy is bypassed for the new URL.
     */
    @Nonnull public static final IScrubSensitiveHeaders DEFAULT_SCRUB_SENSITIVE_HEADERS =
            (requestBuilder, originalUrl, newUrl, proxyResolver) -> {
                Objects.requireNonNull(requestBuilder, "parameter requestBuilder cannot be null");
                Objects.requireNonNull(originalUrl, "parameter originalUrl cannot be null");
                Objects.requireNonNull(newUrl, "parameter newUrl cannot be null");

                // Remove Authorization and Cookie headers if the request's scheme or host changes
                boolean isDifferentHostOrScheme =
                        !newUrl.host().equalsIgnoreCase(originalUrl.host())
                                || !newUrl.scheme().equalsIgnoreCase(originalUrl.scheme());
                if (isDifferentHostOrScheme) {
                    requestBuilder.removeHeader("Authorization");
                    requestBuilder.removeHeader("Cookie");
                }

                // Remove Proxy-Authorization if no proxy is configured or the URL is bypassed
                boolean isProxyInactive = proxyResolver == null || proxyResolver.apply(newUrl) == null;
                if (isProxyInactive) {
                    requestBuilder.removeHeader("Proxy-Authorization");
                }
            };

    /**
     * Create default instance of redirect options, with default values of max redirects and should redirect
     */
    public RedirectHandlerOption() {
        this(DEFAULT_MAX_REDIRECTS, DEFAULT_SHOULD_REDIRECT, DEFAULT_SCRUB_SENSITIVE_HEADERS);
    }

    /**
     * Create an instance with provided values
     * @param maxRedirects Max redirects to occur
     * @param shouldRedirect Should redirect callback called before every redirect
     */
    public RedirectHandlerOption(int maxRedirects, @Nullable final IShouldRedirect shouldRedirect) {
        this(maxRedirects, shouldRedirect, DEFAULT_SCRUB_SENSITIVE_HEADERS);
    }

    /**
     * Create an instance with provided values
     * @param maxRedirects Max redirects to occur
     * @param shouldRedirect Should redirect callback called before every redirect
     * @param scrubSensitiveHeaders Callback to scrub sensitive headers during redirects
     */
    public RedirectHandlerOption(
            int maxRedirects,
            @Nullable final IShouldRedirect shouldRedirect,
            @Nullable final IScrubSensitiveHeaders scrubSensitiveHeaders) {
        if (maxRedirects < 0)
            throw new IllegalArgumentException("Max redirects cannot be negative");
        if (maxRedirects > MAX_REDIRECTS)
            throw new IllegalArgumentException("Max redirect cannot exceed " + MAX_REDIRECTS);

        this.maxRedirects = maxRedirects;
        this.shouldRedirect = shouldRedirect != null ? shouldRedirect : DEFAULT_SHOULD_REDIRECT;
        this.scrubSensitiveHeaders =
                scrubSensitiveHeaders != null
                        ? scrubSensitiveHeaders
                        : DEFAULT_SCRUB_SENSITIVE_HEADERS;
    }

    /**
     * Gets the maximum number of redirects to follow.
     * @return max redirects
     */
    public int maxRedirects() {
        return this.maxRedirects;
    }

    /**
     * Gets the callback evaluating whether a redirect should be followed.
     * @return should redirect
     */
    @Nonnull public IShouldRedirect shouldRedirect() {
        return this.shouldRedirect;
    }

    /**
     * Gets the callback for scrubbing sensitive headers during redirects.
     * @return scrub sensitive headers callback
     */
    @Nonnull public IScrubSensitiveHeaders scrubSensitiveHeaders() {
        return this.scrubSensitiveHeaders;
    }

    /**
     * Helper method to get a proxy resolver from a ProxySelector.
     * @param proxySelector The ProxySelector to use, or null if no proxy is configured
     * @return A function that resolves proxies for a given HttpUrl, or null if no proxy selector is provided
     */
    @Nullable public static Function<HttpUrl, Proxy> getProxyResolver(
            @Nullable final ProxySelector proxySelector) {
        if (proxySelector == null) {
            return null;
        }
        return url -> {
            try {
                URI uri = new URI(url.scheme(), null, url.host(), url.port(), null, null, null);
                List<Proxy> proxies = proxySelector.select(uri);
                if (proxies != null && !proxies.isEmpty()) {
                    Proxy proxy = proxies.get(0);
                    // Return null for DIRECT proxies (no proxy)
                    return proxy.type() == Proxy.Type.DIRECT ? null : proxy;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) RedirectHandlerOption.class;
    }
}
