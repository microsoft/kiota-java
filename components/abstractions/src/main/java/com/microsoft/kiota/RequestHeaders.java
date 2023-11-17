package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

/**
 * A class representing the headers of a request.
 */
public class RequestHeaders extends Headers {
    /** Default constructor */
    public RequestHeaders() {
        super();
    }

    /**
     *  Copy constructor
     * @param requestHeaders The request headers to initialize with.
     */
    public RequestHeaders(@Nonnull RequestHeaders requestHeaders) {
        super(requestHeaders);
    }
}
