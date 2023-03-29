package com.microsoft.kiota;

import java.util.Collections;

import javax.annotation.Nullable;

/** Base class for request configuration */
public abstract class BaseRequestConfiguration {
    /** Request headers */
    @Nullable
    public RequestHeaders headers = new RequestHeaders();
    /** Request options */
    @Nullable
    public java.util.List<RequestOption> options = Collections.emptyList();
}
