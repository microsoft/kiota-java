package com.microsoft.kiota;

import java.util.Collections;
import java.util.List;

/**
 * Base class for request configuration
 */
public abstract class BaseRequestConfiguration {
    /**
     * Default constructor
     */
    public BaseRequestConfiguration() {
        // default empty constructor
    }

    /**
     * Request headers
     */
    public RequestHeaders headers = new RequestHeaders();

    /**
     * Request options
     */
    public List<RequestOption> options = Collections.emptyList();
}
