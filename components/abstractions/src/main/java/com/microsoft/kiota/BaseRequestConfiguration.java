package com.microsoft.kiota;

import jakarta.annotation.Nullable;

import java.util.Collections;

/** Base class for request configuration */
public abstract class BaseRequestConfiguration {
    /**
     * Default constructor
     */
    public BaseRequestConfiguration() {
        // default empty constructor
    }

    /** Request headers */
    @Nullable public RequestHeaders headers = new RequestHeaders();

    /** Request options */
    @Nullable public java.util.List<RequestOption> options = Collections.emptyList();
}
