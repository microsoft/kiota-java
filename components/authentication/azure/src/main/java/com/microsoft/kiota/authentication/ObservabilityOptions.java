package com.microsoft.kiota.authentication;

import jakarta.annotation.Nonnull;

/** Holds the tracing, metrics and logging configuration for the authentication provider adapter */
public class ObservabilityOptions {
    /** default constructor */
    public ObservabilityOptions() {}

    /**
     * Gets the instrumentation name for the tracer
     * @return the instrumentation name for the tracer
     */
    @Nonnull
    public String getTracerInstrumentationName() {
        return "com.microsoft.kiota.authentication:microsoft-kiota-authentication-azure";
    }
}
