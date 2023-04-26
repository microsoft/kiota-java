package com.microsoft.kiota;

import javax.annotation.Nonnull;

/**
 * A class representing the headers of a request.
 */
public class RequestHeaders extends Headers {
    /** Default constructor */
    public RequestHeaders() {
        super();
    }
    
    /** Copy constructor */
    public RequestHeaders(@Nonnull RequestHeaders requestHeaders) {
        super(requestHeaders);
    }
}