package com.microsoft.kiota.http;

import com.microsoft.kiota.RequestOption;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.middleware.AuthorizationHandler;
import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.RedirectHandler;
import com.microsoft.kiota.http.middleware.RetryHandler;
import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.UserAgentHandler;

import com.microsoft.kiota.http.middleware.options.HeadersInspectionOption;
import com.microsoft.kiota.http.middleware.options.ParametersNameDecodingOption;
import com.microsoft.kiota.http.middleware.options.RedirectHandlerOption;
import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;
import com.microsoft.kiota.http.middleware.options.UserAgentHandlerOption;
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

        final Interceptor[] interceptorsOrDefault = createDefaultInterceptors(interceptors);
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
    @Nonnull public static Interceptor[] createDefaultInterceptors( @Nullable Interceptor[] interceptors) {
        if(interceptors == null || interceptors.length == 0){
            return createDefaultInterceptors();
        }
        return interceptors;

    }

    @Nonnull public static Interceptor[] createDefaultInterceptors(@Nullable final List<RequestOption> requestOptions) {

        UrlReplaceHandlerOption uriReplacementOption = null;
        UserAgentHandlerOption userAgentHandlerOption = null;
        RetryHandlerOption retryHandlerOption = null;
        RedirectHandlerOption redirectHandlerOption = null;
        ParametersNameDecodingOption parametersNameDecodingOption = null;
        HeadersInspectionOption headersInspectionHandlerOption = null;

        for (RequestOption option : requestOptions) {
            if (uriReplacementOption == null && option instanceof UrlReplaceHandlerOption) {
                uriReplacementOption = (UrlReplaceHandlerOption) option;
            } else if (retryHandlerOption == null && option instanceof RetryHandlerOption) {
                retryHandlerOption = (RetryHandlerOption) option;
            } else if (redirectHandlerOption == null && option instanceof RedirectHandlerOption) {
                redirectHandlerOption = (RedirectHandlerOption) option;
            } else if (parametersNameDecodingOption == null && option instanceof ParametersNameDecodingOption) {
                parametersNameDecodingOption = (ParametersNameDecodingOption) option;
            } else if (userAgentHandlerOption == null && option instanceof UserAgentHandlerOption) {
                userAgentHandlerOption = (UserAgentHandlerOption) option;
            } else if (headersInspectionHandlerOption == null && option instanceof HeadersInspectionOption) {
                headersInspectionHandlerOption = (HeadersInspectionOption) option;
            }
        }

        final List<Interceptor> handlers = new ArrayList<>();
        handlers.add(uriReplacementOption != null ? new UrlReplaceHandler(uriReplacementOption) : new UrlReplaceHandler());
        handlers.add(retryHandlerOption != null ? new RetryHandler(retryHandlerOption) : new RetryHandler());
        handlers.add(redirectHandlerOption != null ? new RedirectHandler(redirectHandlerOption) : new RedirectHandler());
        handlers.add(parametersNameDecodingOption != null ? new ParametersNameDecodingHandler(parametersNameDecodingOption) : new ParametersNameDecodingHandler());
        handlers.add(userAgentHandlerOption != null ? new UserAgentHandler(userAgentHandlerOption) : new UserAgentHandler());
        handlers.add(headersInspectionHandlerOption != null ? new HeadersInspectionHandler(headersInspectionHandlerOption) : new HeadersInspectionHandler());

        return handlers.toArray(new Interceptor[0]);
    }

    /**
     * Creates the default interceptors for the client.
     * @return an array of interceptors.
     */
    @Nonnull public static List<Interceptor> createDefaultInterceptorsAsList() {
        return new ArrayList<>(Arrays.asList(createDefaultInterceptors()));
    }

}
