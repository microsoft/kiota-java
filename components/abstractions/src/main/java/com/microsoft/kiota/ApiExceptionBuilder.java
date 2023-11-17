package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

/** Builder class for ApiException. */
public class ApiExceptionBuilder {

    private ApiException value = null;

    /**
     * Constructs an empty ApiExceptionBuilder
     */
    public ApiExceptionBuilder() {}

    /**
     * Constructs an ApiExceptionBuilder starting from a base ApiException
     * @param builder A builder for the ApiException to be used as a base.
     */
    public ApiExceptionBuilder(@Nonnull final Supplier<Parsable> builder) {
        Objects.requireNonNull(builder);
        final Parsable error = builder.get();
        if (error instanceof ApiException) {
            value = (ApiException) error;
        } else {
            value =
                    new ApiExceptionBuilder()
                            .withMessage("\"unexpected error type \"" + error.getClass().getName())
                            .build();
        }
    }

    /**
     * Assign the message to the builder
     * @param message The message to be attached to this ApiException.
     * @return The builder object.
     */
    @Nonnull public ApiExceptionBuilder withMessage(@Nonnull String message) {
        Objects.requireNonNull(message);
        if (value == null) {
            value = new ApiException(message);
        } else {
            value = new ApiException(message, value.getCause());
        }
        return this;
    }

    /**
     * Assign the Throwable cause of the Exception to the builder
     * @param exception The Throwable to be used as Cause for this ApiException.
     * @return The builder object.
     */
    @Nonnull public ApiExceptionBuilder withThrowable(@Nonnull Throwable exception) {
        Objects.requireNonNull(exception);
        if (value == null) {
            value = new ApiException(exception);
        } else {
            value = new ApiException(value.getMessage(), value.getCause());
        }
        return this;
    }

    /**
     * Assign the response status code to the builder
     * @param responseStatusCode an int representing the response status code.
     * @return The builder object.
     */
    @Nonnull public ApiExceptionBuilder withResponseStatusCode(int responseStatusCode) {
        if (value == null) {
            value = new ApiException();
        }
        value.setResponseStatusCode(responseStatusCode);
        return this;
    }

    /**
     * Assign the response headers to the builder
     * @param responseHeaders the response headers to be added to this ApiException.
     * @return The builder object.
     */
    @Nonnull public ApiExceptionBuilder withResponseHeaders(@Nonnull ResponseHeaders responseHeaders) {
        if (value == null) {
            value = new ApiException();
        }
        value.setResponseHeaders(responseHeaders);
        return this;
    }

    /**
     * Build and return an instance of ApiException
     * @return The built ApiException.
     */
    @Nonnull public ApiException build() {
        ApiException result = value;
        value = null;
        return result;
    }
}
