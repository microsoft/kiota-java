package com.microsoft.kiota.http;

import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.RedirectHandler;
import com.microsoft.kiota.http.middleware.RetryHandler;
import com.microsoft.kiota.http.middleware.UserAgentHandler;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.time.Duration;

/** This class is used to build the HttpClient instance used by the core service. */
public class KiotaClientFactory {
    private KiotaClientFactory() {}

    /**
     * Creates an OkHttpClient Builder with the default configuration and middleware.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull public static OkHttpClient.Builder create() {
        return create(null);
    }

    /**
     * Creates an OkHttpClient Builder with the default configuration and middleware.
     * @param interceptors The interceptors to add to the client. Will default to createDefaultInterceptors() if null.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull public static OkHttpClient.Builder create(@Nullable final Interceptor[] interceptors) {
        final OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .connectTimeout(Duration.ofSeconds(100))
                        .readTimeout(Duration.ofSeconds(100))
                        .callTimeout(
                                Duration.ofSeconds(
                                        100)); // TODO configure the default client options.

        final Interceptor[] interceptorsOrDefault =
                interceptors != null ? interceptors : createDefaultInterceptors();
        for (final Interceptor interceptor : interceptorsOrDefault) {
            builder.addInterceptor(interceptor);
        }
        return builder;
    }

    /**
     * Creates the default interceptors for the client.
     * @return an array of interceptors.
     */
    @Nonnull public static Interceptor[] createDefaultInterceptors() {
        return new Interceptor[] {
            new RedirectHandler(),
            new RetryHandler(),
            new ParametersNameDecodingHandler(),
            new UserAgentHandler(),
            new HeadersInspectionHandler()
        };
    }
}
