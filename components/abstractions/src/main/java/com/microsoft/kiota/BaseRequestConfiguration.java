package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

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
    @Nonnull public RequestHeaders headers = new RequestHeaders();

    /**
     * Request options
     */
    @Nonnull public List<RequestOption> options = Collections.emptyList();
}
