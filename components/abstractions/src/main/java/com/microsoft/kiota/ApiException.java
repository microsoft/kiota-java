package com.microsoft.kiota;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/** Parent type for exceptions thrown by the client when receiving failed responses to its requests. */
public class ApiException extends RuntimeException {
    /** {@inheritDoc} */
    public ApiException() {
        super();
    }

    /** {@inheritDoc} */
    public ApiException(@Nonnull final String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public ApiException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }

    /** {@inheritDoc} */
    public ApiException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /** The HTTP status code  for the response*/
    private int responseStatusCode;

    /**
     * Gets the HTTP response status code
     * @return The response status code from the failed response.
     */
    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    /**
     * Sets the HTTP response status code
     * @param responseStatusCode The response status code to set.
     */
    protected void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    /** The HTTP response headers for the error response*/
    @Nonnull private ResponseHeaders responseHeaders = new ResponseHeaders();

    /**
     * Gets the HTTP response headers for the error response
     * @return The response headers collections from the failed response.
     */
    @Nonnull public ResponseHeaders getResponseHeaders() {
        return new ResponseHeaders(responseHeaders);
    }

    /**
     * Sets the HTTP response headers for the error response
     * @param responseHeaders The response headers collections to set.
     */
    protected void setResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        Objects.requireNonNull(responseHeaders);
        this.responseHeaders = new ResponseHeaders(responseHeaders);
    }
}
