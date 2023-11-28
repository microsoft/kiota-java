package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.middleware.options.TelemetryHandlerOption;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * TelemetryHandler implementation using OkHttp3
 */
public class TelemetryHandler implements Interceptor {
    @Nonnull private final TelemetryHandlerOption _telemetryHandlerOption;

    /**
     * TelemetryHandler no param constructor
     */
    public TelemetryHandler() {
        this(null);
    }

    /**
     * TelemetryHandler constructor with passed in options.
     * @param telemetryHandlerOption The user specified telemetryHandlerOptions
     */
    public TelemetryHandler(@Nullable TelemetryHandlerOption telemetryHandlerOption) {
        if (telemetryHandlerOption == null) {
            this._telemetryHandlerOption = new TelemetryHandlerOption();
        } else {
            this._telemetryHandlerOption = telemetryHandlerOption;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("UnknownNullness")
    @Nonnull public Response intercept(Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        final Request request = chain.request();

        TelemetryHandlerOption telemetryHandlerOption = request.tag(TelemetryHandlerOption.class);
        if (telemetryHandlerOption == null) {
            telemetryHandlerOption = this._telemetryHandlerOption;
        }

        // Simply forward request if TelemetryConfigurator is set to null intentionally.
        final Function<Request, Request> telemetryConfigurator =
                telemetryHandlerOption.telemetryConfigurator;
        if (telemetryConfigurator == null) {
            return chain.proceed(request);
        }

        // Use the TelemetryConfigurator set by the user to enrich the request as desired.
        Request enrichedRequest = telemetryConfigurator.apply(request);
        return chain.proceed(enrichedRequest);
    }
}
