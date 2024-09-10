package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.ContinuousAccessEvaluationClaims;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.microsoft.kiota.http.TelemetrySemanticConventions.HTTP_REQUEST_RESEND_COUNT;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AuthorizationHandler implements Interceptor {

    private final BaseBearerTokenAuthenticationProvider authenticationProvider;
    private static final String authorizationHeaderKey = "Authorization";

    /**
     * Instantiates a new AuthorizationHandler.
     * @param authenticationProvider the authentication provider.
     */
    public AuthorizationHandler(BaseBearerTokenAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        final Request request = chain.request();

        final Span span =
                ObservabilityHelper.getSpanForRequest(request, "AuthorizationHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.authorization.enable", true);
        }

        try {
            // Auth provider already added auth header
            if (request.headers().names().contains(authorizationHeaderKey)) {
                span.setAttribute("com.microsoft.kiota.handler.authorization.token_present", true);
                return chain.proceed(request);
            }

            authenticateRequest(request, null, span);
            Response response = chain.proceed(chain.request());

            if (response != null && response.code() != HttpURLConnection.HTTP_UNAUTHORIZED) {
                return response;
            }

            // Attempt CAE claims challenge
            String claims = ContinuousAccessEvaluationClaims.getClaimsFromResponse(response);
            if (claims == null || claims.isEmpty()) {
                return response;
            }

            span.addEvent("com.microsoft.kiota.handler.authorization.challenge_received");

            // We cannot replay one-shot requests after claims challenge
            boolean isRequestBodyOneShot = request != null && request.body() != null && request.body().isOneShot();
            if (isRequestBodyOneShot) {
                return response;
            }

            response.close();
            final HashMap<String, Object> additionalContext = new HashMap<>();
            additionalContext.put("claims", claims);
            // Retry claims challenge only once
            authenticateRequest(request, additionalContext, span);
            span.setAttribute(HTTP_REQUEST_RESEND_COUNT, 1);
            return chain.proceed(request);
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
    }

    private void authenticateRequest(
            @Nonnull Request request,
            @Nullable Map<String, Object> additionalAuthenticationContext,
            @Nonnull Span span) {
        final RequestInformation requestInformation = getRequestInformation(request);
        authenticationProvider.authenticateRequest(
                requestInformation, additionalAuthenticationContext);
        // Update native request with headers added to requestInformation
        if (requestInformation.headers.containsKey(authorizationHeaderKey)) {
            span.setAttribute("com.microsoft.kiota.handler.authorization.token_obtained", true);
            Set<String> authorizationHeaderValues =
                    requestInformation.headers.get(authorizationHeaderKey);
            if (!authorizationHeaderValues.isEmpty()) {
                Request.Builder requestBuilder = request.newBuilder();
                for (String value : authorizationHeaderValues) {
                    requestBuilder.addHeader(authorizationHeaderKey, value);
                }
            }
        }
    }

    private RequestInformation getRequestInformation(final Request request) {
        RequestInformation requestInformation = new RequestInformation();
        requestInformation.setUri(request.url().uri());
        for (String headerName : request.headers().names()) {
            requestInformation.headers.add(headerName, request.header(headerName));
        }
        return requestInformation;
    }
}
