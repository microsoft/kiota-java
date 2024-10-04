package com.microsoft.kiota.http;

import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.middleware.AuthorizationHandler;
import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.RedirectHandler;
import com.microsoft.kiota.http.middleware.RetryHandler;
import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.UserAgentHandler;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** This class is used to build the HttpClient instance used by the core service. */
public class KiotaClientFactory {
    private KiotaClientFactory() {}

    /**
     * Creates an OkHttpClient Builder with the default configuration and middleware.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull public static OkHttpClient.Builder create() {
        return create(createDefaultInterceptors());
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
                interceptors != null ? createOveridableDefaultInterceptors( interceptors ): createDefaultInterceptors();
        for (final Interceptor interceptor : interceptorsOrDefault) {
            builder.addInterceptor(interceptor);
        }
        return builder;
    }

    /**
     * Creates an OkHttpClient Builder with the default configuration and middleware.
     * @param interceptors The interceptors to add to the client. Will default to createDefaultInterceptors() if null.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull public static OkHttpClient.Builder create(@Nullable final List<Interceptor> interceptors) {
        if (interceptors == null) {
            return create();
        }
        return create(
                (new ArrayList<>(interceptors)).toArray(new Interceptor[interceptors.size()]));
    }

    /**
     * Creates an OkHttpClient Builder with the default configuration and middleware including the AuthorizationHandler.
     * @param authenticationProvider authentication provider to use for the AuthorizationHandler.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull public static OkHttpClient.Builder create(
            @Nonnull final BaseBearerTokenAuthenticationProvider authenticationProvider) {
        ArrayList<Interceptor> interceptors = new ArrayList<>(createDefaultInterceptorsAsList());
        interceptors.add(new AuthorizationHandler(authenticationProvider));
        return create(interceptors);
    }

    /**
     * Creates the default interceptors for the client.
     * @return an array of interceptors.
     */
    @Nonnull public static Interceptor[] createDefaultInterceptors() {
        return new Interceptor[] {
            new UrlReplaceHandler(),
            new RedirectHandler(),
            new RetryHandler(),
            new ParametersNameDecodingHandler(),
            new UserAgentHandler(),
            new HeadersInspectionHandler()
        };
    }

    /**
     * Creates the default interceptors for the client.
     * @return an array of interceptors.
     */
    @Nonnull public static Interceptor[] createOveridableDefaultInterceptors( @Nonnull Interceptor[] interceptors) {

        List<Interceptor> handlers = new ArrayList<>(Arrays.asList(interceptors));
        Set<String> defaultHandlerTypes = getDefaultHandler();

        for (Interceptor interceptor : interceptors) {
            defaultHandlerTypes.remove(interceptor.getClass().toString());
        }
        // Add any remaining default interceptors
        for (String handlerType : defaultHandlerTypes) {

            handlers.add(createHandler(handlerType));
        }

        return handlers.toArray(new Interceptor[0]);
    }




    /**
     * Creates the default interceptors for the client.
     * @return an array of interceptors.
     */
    @Nonnull public static List<Interceptor> createDefaultInterceptorsAsList() {
        return new ArrayList<>(Arrays.asList(createDefaultInterceptors()));
    }


    /**
     * Gets the default handler types.
     *
     * @return A list of all the default handlers classnames
     *
     */
    private static Set<String> getDefaultHandler() {
        return new HashSet<>(Arrays.asList(
            UrlReplaceHandler.class.toString(),
            RedirectHandler.class.toString(),
            RetryHandler.class.toString(),
            ParametersNameDecodingHandler.class.toString(),
            UserAgentHandler.class.toString(),
            HeadersInspectionHandler.class.toString()));
    }
    private static Interceptor createHandler(String handlerType) {
        switch (handlerType) {
            case "class com.microsoft.kiota.http.middleware.RetryHandler":
                return new RetryHandler();
            case "class com.microsoft.kiota.http.middleware.RedirectHandler":
                return new RedirectHandler();
            case "class com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler":
                return new ParametersNameDecodingHandler();
            case "class com.microsoft.kiota.http.middleware.UserAgentHandler":
                return new UserAgentHandler();
            case "class com.microsoft.kiota.http.middleware.HeadersInspectionHandler":
                return new HeadersInspectionHandler();
            case "class com.microsoft.kiota.http.middleware.UrlReplaceHandler":
                return new UrlReplaceHandler();
            default:
                return null; // Handle unknown types as necessary
        }
    }
}
