package com.microsoft.kiota;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Request option that can be used to provide a callback and handle the raw response */
public class ResponseHandlerOption implements RequestOption {
    /** Creates a new instance of the option */
    public ResponseHandlerOption() {
        // default constructor
    }

    private ResponseHandler responseHandler;

    /**
     * Gets the response handler callback
     * @return the response handler callback
     */
    @Nullable public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    /**
     * Sets the response handler callback
     * @param value the response handler callback
     */
    public void setResponseHandler(@Nullable final ResponseHandler value) {
        responseHandler = value;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) ResponseHandlerOption.class;
    }
}
