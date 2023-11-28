package com.microsoft.kiota.http.middleware;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DO NOT USE IN PRODUCTION
 * interceptor that randomly fails the responses for unit testing purposes
 */
public class ChaosHandler implements Interceptor {
    /** Creates a new instance of the chaos handler */
    public ChaosHandler() {}

    /**
     * constant string being used
     */
    private static final String RETRY_AFTER = "Retry-After";

    /**
     * Denominator for the failure rate (i.e. 1/X)
     */
    private static final int failureRate = 3;

    /**
     * default value to return on retry after
     */
    private static final String retryAfterValue = "10";

    /**
     * body to respond on failed requests
     */
    private static final String responseBody =
            "{\"error\": {\"code\": \"TooManyRequests\",\"innerError\": {\"code\":"
                + " \"429\",\"date\": \"2020-08-18T12:51:51\",\"message\": \"Please retry"
                + " after\",\"request-id\": \"94fb3b52-452a-4535-a601-69e0a90e3aa2\",\"status\":"
                + " \"429\"},\"message\": \"Please retry again later.\"}}";

    /**
     * Too many requests status code
     */
    public static final int MSClientErrorCodeTooManyRequests = 429;

    /** The key for the open telemetry event */
    public static final String chaosHandlerTriggeredEventKey =
            "com.microsoft.kiota.chaos_handler_triggered";

    @Override
    @Nonnull public Response intercept(@Nonnull final Chain chain) throws IOException {
        Request request = chain.request();
        final Span span = ObservabilityHelper.getSpanForRequest(request, "ChaosHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.chaos.enable", true);
        }

        try {
            final int dice = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

            if (dice % failureRate == 0) {
                span.addEvent(chaosHandlerTriggeredEventKey);
                return new Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(MSClientErrorCodeTooManyRequests)
                        .message("Too Many Requests")
                        .addHeader(RETRY_AFTER, retryAfterValue)
                        .body(ResponseBody.create(responseBody, MediaType.get("application/json")))
                        .build();
            } else {
                if (span != null) {
                    request = request.newBuilder().tag(Span.class, span).build();
                }
                return chain.proceed(request);
            }
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
    }
}
