package com.microsoft.kiota;

import java.util.Objects;

import javax.annotation.Nonnull;

/** Parent type for exceptions thrown by the client when receiving failed responses to its requests. */
public class ApiException extends Exception {
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
    public int responseStatusCode;

    /** The HTTP response headers for the error response*/
    @Nonnull
    private ResponseHeaders responseHeaders = new ResponseHeaders();

    /** Gets the HTTP response headers for the error response */
    @Nonnull
    public ResponseHeaders getResponseHeaders() {
        return new ResponseHeaders(responseHeaders);
    }

    /** Sets the HTTP response headers for the error response */
    public void setResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        Objects.requireNonNull(responseHeaders);
        this.responseHeaders = new ResponseHeaders(responseHeaders);
    }

}
