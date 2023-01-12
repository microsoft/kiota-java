package com.microsoft.kiota.authentication;

import javax.annotation.Nonnull;

/** Holds the tracing, metrics and logging configuration for the authentication provider adapter */
public class ObservabilityOptions {
	@Nonnull
	/** Gets the instrumentation name to use for tracing */
	public String getTracerInstrumentationName() {
		return "com.microsoft.kiota.authentication:microsoft-kiota-authentication-azure";
	}
}
