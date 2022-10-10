package com.microsoft.kiota;

import javax.annotation.Nonnull;

/** Parent type for exceptions thrown by the client when receiving failed responses to its requests. */
public class ApiException extends Exception {
    /** {@inheritdoc} */
    public ApiException() {
        super();
    }
    /** {@inheritdoc} */
    public ApiException(@Nonnull final String message) {
        super(message);
    }
    /** {@inheritdoc} */
    public ApiException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
    /** {@inheritdoc} */
    public ApiException(@Nonnull final Throwable cause) {
        super(cause);
    }
}
