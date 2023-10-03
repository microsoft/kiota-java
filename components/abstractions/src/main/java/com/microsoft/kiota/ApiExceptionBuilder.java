package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Builder class for ApiException. */
public class ApiExceptionBuilder {

    private ApiException value = null;
    public ApiExceptionBuilder() {
    }
    public ApiExceptionBuilder(ApiException base) {
        value = base;
    }
    public ApiExceptionBuilder withMessage(@Nonnull String message) {
        Objects.requireNonNull(message);
        if (value == null) {
            value = new ApiException(message);
        } else {
            value = new ApiException(message, value.getCause());
        }
        return this;
    }
    public ApiExceptionBuilder withThrowable(@Nonnull Throwable exception) {
        Objects.requireNonNull(exception);
        if (value == null) {
            value = new ApiException(exception);
        } else {
            value = new ApiException(value.getMessage(), value.getCause());
        }
        return this;
    }
    public ApiExceptionBuilder withResponseStatusCode(int responseStatusCode) {
        if (value == null) {
            value = new ApiException();
        }
        value.setResponseStatusCode(responseStatusCode);
        return this;
    }
    public ApiExceptionBuilder withResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        if (value == null) {
            value = new ApiException();
        }
        value.setResponseHeaders(responseHeaders);
        return this;
    }
    public ApiException build() {
        return value;
    }
}
