package com.microsoft.kiota;

import javax.annotation.Nonnull;

/**
 * A class representing the headers of a request.
 */
public class ResponseHeaders extends Headers {
    /** Default constructor */
    public ResponseHeaders() {
        super();
    }

    /** Copy constructor */
    public ResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        super(responseHeaders);
    }
}