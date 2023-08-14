package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

/**
 * A class representing the headers of a request.
 */
public class ResponseHeaders extends Headers {
    /** Default constructor */
    public ResponseHeaders() {
        super();
    }

    /**
     *  Copy constructor
     * @param responseHeaders The response headers to initialize with.
     */
    public ResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        super(responseHeaders);
    }
}