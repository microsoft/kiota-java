package com.microsoft.kiota.http.middleware.options;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.RequestOption;

import okhttp3.Request;

/**
 * TelemetryHandlerOption class
 */
public class TelemetryHandlerOption implements RequestOption {
    /** Creates a new instance of the TelemetryHandlerOption class */
    public TelemetryHandlerOption() {}

    /**
    * A delegate which can be called to configure the Request with desired telemetry values.
    */
    @Nullable
    public Function<Request, Request> telemetryConfigurator = (request) -> request;

	/* @inheritdoc */
    @Override
    @Nonnull
	@SuppressWarnings("unchecked")
    public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) TelemetryHandlerOption.class; 
    }
}
