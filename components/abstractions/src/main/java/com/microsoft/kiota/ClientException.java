package com.microsoft.kiota;

import jakarta.annotation.Nonnull;

/** An error in client-side request or response processing, rather than an API error response. */
public class ClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public ClientException() {
        super();
    }

    /** {@inheritDoc} */
    public ClientException(@Nonnull final String message) {
        super(message);
    }

    /** {@inheritDoc} */
    public ClientException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }

    /** {@inheritDoc} */
    public ClientException(@Nonnull final Throwable cause) {
        super(cause);
    }
}
