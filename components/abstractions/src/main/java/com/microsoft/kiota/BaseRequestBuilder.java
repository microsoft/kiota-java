package com.microsoft.kiota;

import java.util.HashMap;
import java.util.Objects;

import jakarta.annotation.Nonnull;

/** Base class for request builders */
public abstract class BaseRequestBuilder {
    /** Path parameters for the request */
    @Nonnull
    protected HashMap<String, Object> pathParameters;
    /** The request adapter to use to execute the requests. */
    @Nonnull
    protected RequestAdapter requestAdapter;
    /** Url template to use to build the URL for the current request builder */
    @Nonnull
    protected String urlTemplate;
    /**
     * Instantiates a new BaseRequestBuilder and sets the default values.
     * @param requestAdapter The request adapter to use to execute the requests.
     * @param urlTemplate Url template to use to build the URL for the current request builder
     */
    protected BaseRequestBuilder(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String urlTemplate) {
        this(requestAdapter, urlTemplate, new HashMap<>());
    }
    /**
     * Instantiates a new BaseRequestBuilder and sets the default values.
     * @param requestAdapter The request adapter to use to execute the requests.
     * @param urlTemplate Url template to use to build the URL for the current request builder
     * @param pathParameters Path parameters for the request
     */
    protected BaseRequestBuilder(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String urlTemplate, @Nonnull final HashMap<String, Object> pathParameters) {
        this.requestAdapter = Objects.requireNonNull(requestAdapter);
        this.urlTemplate = Objects.requireNonNull(urlTemplate);
        this.pathParameters = new HashMap<>(Objects.requireNonNull(pathParameters));
    }
    /**
     * Instantiates a new BaseRequestBuilder and sets the default values.
     * @param requestAdapter The request adapter to use to execute the requests.
     * @param urlTemplate Url template to use to build the URL for the current request builder
     * @param rawUrl the raw url to use for the request
     */
    protected BaseRequestBuilder(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String urlTemplate, @Nonnull final String rawUrl) {
        this(requestAdapter, urlTemplate);
        this.pathParameters.put(RequestInformation.RAW_URL_KEY, Objects.requireNonNull(rawUrl));
    }
}
