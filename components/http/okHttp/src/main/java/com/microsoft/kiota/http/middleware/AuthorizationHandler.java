package com.microsoft.kiota.http.middleware;

import static com.microsoft.kiota.http.TelemetrySemanticConventions.HTTP_REQUEST_RESEND_COUNT;

import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.ContinuousAccessEvaluationClaims;
import com.microsoft.kiota.http.ObservabilityOptions;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This interceptor is responsible for adding the Authorization header to the request
 * if the header is not already present. It also handles Continuous Access Evaluation (CAE) claims
 * challenges if the token request was made using this interceptor. It does this using the provided AuthenticationProvider
 */
public class AuthorizationHandler implements Interceptor {

    @Nonnull private final BaseBearerTokenAuthenticationProvider authenticationProvider;
    private static final String authorizationHeaderKey = "Authorization";

    /**
     * Instantiates a new AuthorizationHandler.
     * @param authenticationProvider the authentication provider.
     */
    public AuthorizationHandler(
            @Nonnull final BaseBearerTokenAuthenticationProvider authenticationProvider) {
        this.authenticationProvider =
                Objects.requireNonNull(
                        authenticationProvider, "AuthenticationProvider cannot be null");
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        final Request request = chain.request();

        final Span span =
                GlobalOpenTelemetry.getTracer(
                                (new ObservabilityOptions()).getTracerInstrumentationName())
                        .spanBuilder("AuthorizationHandler_Intercept")
                        .startSpan();
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.authorization.enable", true);
        }

        try {
            // Auth provider already added auth header
            if (request.headers().names().contains(authorizationHeaderKey)) {
                if (span != null)
                    span.setAttribute(
                            "com.microsoft.kiota.handler.authorization.token_present", true);
                return chain.proceed(request);
            }

            final HashMap<String, Object> additionalContext = new HashMap<>();
            additionalContext.put("parent-span", span);
            final Request authenticatedRequest =
                    authenticateRequest(request, additionalContext, span);
            final Response response = chain.proceed(authenticatedRequest);

            if (response != null && response.code() != HttpURLConnection.HTTP_UNAUTHORIZED) {
                return response;
            }

            // Attempt CAE claims challenge
            final String claims = ContinuousAccessEvaluationClaims.getClaimsFromResponse(response);
            if (claims == null || claims.isEmpty()) {
                return response;
            }

            span.addEvent("com.microsoft.kiota.handler.authorization.challenge_received");

            // We cannot replay one-shot requests after claims challenge
            RequestBody requestBody = request.body();
            if (requestBody != null && requestBody.isOneShot()) {
                return response;
            }

            response.close();
            additionalContext.put("claims", claims);
            // Retry claims challenge only once
            span.setAttribute(HTTP_REQUEST_RESEND_COUNT, 1);
            final Request authenticatedRequestAfterCAE =
                    authenticateRequest(request, additionalContext, span);
            return chain.proceed(authenticatedRequestAfterCAE);
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
    }

    private @Nonnull Request authenticateRequest(
            @Nonnull final Request request,
            @Nullable final Map<String, Object> additionalAuthenticationContext,
            @Nonnull final Span span) {

        final AccessTokenProvider accessTokenProvider =
                authenticationProvider.getAccessTokenProvider();
        if (!accessTokenProvider.getAllowedHostsValidator().isUrlHostValid(request.url().uri())) {
            return request;
        }
        final String accessToken =
                accessTokenProvider.getAuthorizationToken(
                        request.url().uri(), additionalAuthenticationContext);
        if (accessToken != null && !accessToken.isEmpty()) {
            span.setAttribute("com.microsoft.kiota.handler.authorization.token_obtained", true);
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.addHeader(authorizationHeaderKey, "Bearer " + accessToken);
            return requestBuilder.build();
        }
        return request;
    }
}
