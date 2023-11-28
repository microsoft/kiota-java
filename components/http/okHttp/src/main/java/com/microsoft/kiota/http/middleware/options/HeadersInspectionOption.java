package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestHeaders;
import com.microsoft.kiota.RequestOption;
import com.microsoft.kiota.ResponseHeaders;

import jakarta.annotation.Nonnull;

/**
 * The options to be passed to the headers inspection middleware.
 */
public class HeadersInspectionOption implements RequestOption {
    private boolean inspectRequestHeaders;

    /**
     * Gets whether to inspect request headers
     * @return Whether to inspect request headers
     */
    public boolean getInspectRequestHeaders() {
        return inspectRequestHeaders;
    }

    /**
     * Sets whether to inspect request headers
     * @param inspectRequestHeaders Whether to inspect request headers
     */
    public void setInspectRequestHeaders(boolean inspectRequestHeaders) {
        this.inspectRequestHeaders = inspectRequestHeaders;
    }

    private boolean inspectResponseHeaders;

    /**
     * Gets whether to inspect response headers
     * @return Whether to inspect response headers
     */
    public boolean getInspectResponseHeaders() {
        return inspectResponseHeaders;
    }

    /**
     * Sets whether to inspect response headers
     * @param inspectResponseHeaders Whether to inspect response headers
     */
    public void setInspectResponseHeaders(boolean inspectResponseHeaders) {
        this.inspectResponseHeaders = inspectResponseHeaders;
    }

    private final RequestHeaders requestHeaders = new RequestHeaders();
    private final ResponseHeaders responseHeaders = new ResponseHeaders();

    /**
     * Create default instance of headers inspection options, with default values of inspectRequestHeaders and inspectResponseHeaders.
     */
    public HeadersInspectionOption() {
        this(false, false);
    }

    /**
     * Create an instance with provided values
     * @param shouldInspectResponseHeaders Whether to inspect response headers
     * @param shouldInspectRequestHeaders Whether to inspect request headers
     */
    public HeadersInspectionOption(
            final boolean shouldInspectRequestHeaders, final boolean shouldInspectResponseHeaders) {
        this.inspectResponseHeaders = shouldInspectResponseHeaders;
        this.inspectRequestHeaders = shouldInspectRequestHeaders;
    }

    /**
     * Get the request headers
     * @return The request headers
     */
    @Nonnull public RequestHeaders getRequestHeaders() {
        return this.requestHeaders;
    }

    /**
     * Get the response headers
     * @return The response headers
     */
    @Nonnull public ResponseHeaders getResponseHeaders() {
        return this.responseHeaders;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) HeadersInspectionOption.class;
    }
}
