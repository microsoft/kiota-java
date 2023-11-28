package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.middleware.options.IShouldRetry;
import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * The middleware responsible for retrying requests when they fail because of transient issues
 */
public class RetryHandler implements Interceptor {

    @Nonnull private RetryHandlerOption mRetryOption;

    /**
     * Header name to track the retry attempt number
     */
    private static final String RETRY_ATTEMPT_HEADER = "Retry-Attempt";

    /**
     * Header name for the retry after information
     */
    private static final String RETRY_AFTER = "Retry-After";

    /**
     * Too many requests status code
     */
    public static final int MSClientErrorCodeTooManyRequests = 429;

    /**
     * Service unavailable status code
     */
    public static final int MSClientErrorCodeServiceUnavailable = 503;

    /**
     * Gateway timeout status code
     */
    public static final int MSClientErrorCodeGatewayTimeout = 504;

    /**
     * One second as milliseconds
     */
    private static final long DELAY_MILLISECONDS = 1000;

    /**
     * Initialize retry handler with retry option
     * @param retryOption Create Retry handler using retry option
     */
    public RetryHandler(@Nullable final RetryHandlerOption retryOption) {
        if (retryOption == null) {
            this.mRetryOption = new RetryHandlerOption();
        } else {
            this.mRetryOption = retryOption;
        }
    }

    /**
     * Initialize retry handler with default retry option
     */
    public RetryHandler() {
        this(null);
    }

    boolean retryRequest(
            @Nonnull final Response response,
            int executionCount,
            @Nonnull final Request request,
            @Nonnull final RetryHandlerOption retryOption) {

        // Should retry option
        // Use should retry common for all requests
        IShouldRetry shouldRetryCallback = null;
        if (retryOption != null) {
            shouldRetryCallback = retryOption.shouldRetry();
        }

        boolean shouldRetry = false;
        // Status codes 429 503 504
        int statusCode = response.code();
        // Only requests with payloads that are buffered/rewindable are supported.
        // Payloads with forward only streams will be have the responses returned
        // without any retry attempt.
        shouldRetry =
                shouldRetryCallback != null
                        && executionCount <= retryOption.maxRetries()
                        && checkStatus(statusCode)
                        && isBuffered(request)
                        && shouldRetryCallback.shouldRetry(
                                retryOption.delay(), executionCount, request, response);

        if (shouldRetry) {
            long retryInterval = getRetryAfter(response, retryOption.delay(), executionCount);
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return shouldRetry;
    }

    /**
     * Get retry after in milliseconds
     * @param response Response
     * @param delay Delay in seconds
     * @param executionCount Execution count of retries
     * @return Retry interval in milliseconds
     */
    long getRetryAfter(Response response, long delay, int executionCount) {
        String retryAfterHeader = response.header(RETRY_AFTER);
        double retryDelay = -1;
        if (retryAfterHeader != null) {
            retryDelay = tryParseTimeHeader(retryAfterHeader);
            if (retryDelay == -1) {
                retryDelay = tryParseDateHeader(retryAfterHeader);
            }
        } else if (retryDelay == -1) {
            retryDelay = exponentialBackOffDelay(delay, executionCount);
        }
        return (long) Math.min(retryDelay, RetryHandlerOption.MAX_DELAY * DELAY_MILLISECONDS);
    }

    double tryParseTimeHeader(String retryAfterHeader) {
        double retryDelay = -1;
        try {
            retryDelay = Integer.parseInt(retryAfterHeader) * DELAY_MILLISECONDS;
        } catch (NumberFormatException e) {
            return retryDelay;
        }
        return retryDelay;
    }

    double tryParseDateHeader(String retryAfterHeader) {
        double retryDelay = -1;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            Instant headerTime = Instant.from(formatter.parse(retryAfterHeader));
            Instant now = Instant.now();
            if (headerTime.isAfter(now)) {
                retryDelay = ChronoUnit.MILLIS.between(now, headerTime);
            }
        } catch (DateTimeParseException e) {
            return retryDelay;
        }
        return retryDelay;
    }

    private double exponentialBackOffDelay(double delay, int executionCount) {
        double retryDelay = RetryHandlerOption.DEFAULT_DELAY * DELAY_MILLISECONDS;
        retryDelay = (double) ((Math.pow(2.0, (double) executionCount) - 1) * 0.5);
        retryDelay = (executionCount < 2 ? delay : retryDelay + delay) + (double) Math.random();
        retryDelay *= DELAY_MILLISECONDS;
        return retryDelay;
    }

    boolean checkStatus(int statusCode) {
        return (statusCode == MSClientErrorCodeTooManyRequests
                || statusCode == MSClientErrorCodeServiceUnavailable
                || statusCode == MSClientErrorCodeGatewayTimeout);
    }

    boolean isBuffered(final Request request) {
        final String methodName = request.method();

        final boolean isHTTPMethodPutPatchOrPost =
                methodName.equalsIgnoreCase("POST")
                        || methodName.equalsIgnoreCase("PUT")
                        || methodName.equalsIgnoreCase("PATCH");

        final RequestBody requestBody = request.body();
        if (isHTTPMethodPutPatchOrPost && requestBody != null) {
            try {
                return requestBody.contentLength() != -1L;
            } catch (IOException ex) {
                // expected
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the retry options in use by the handler.
     * @return the retry options in use by the handler.
     */
    @Nonnull public RetryHandlerOption getRetryOptions() {
        return this.mRetryOption;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("UnknownNullness")
    @Nonnull public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        Request request = chain.request();
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        final Span span = ObservabilityHelper.getSpanForRequest(request, "RetryHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.retry.enable", true);
        }
        try {
            if (span != null) {
                request = request.newBuilder().tag(Span.class, span).build();
            }
            Response response = chain.proceed(request);
            if (response == null)
                throw new RuntimeException("unable to get a response from the chain");

            // Use should retry pass along with this request
            RetryHandlerOption retryOption = request.tag(RetryHandlerOption.class);
            if (retryOption == null) {
                retryOption = mRetryOption;
            }

            int executionCount = 1;
            while (retryRequest(response, executionCount, request, retryOption)) {
                final Request.Builder builder =
                        request.newBuilder()
                                .addHeader(RETRY_ATTEMPT_HEADER, String.valueOf(executionCount));
                if (span != null) {
                    builder.tag(Span.class, span);
                }
                request = builder.build();
                if (request == null) {
                    throw new IllegalArgumentException("request cannot be null");
                }
                executionCount++;
                final ResponseBody body = response.body();
                if (body != null) body.close();
                response.close();
                final Span retrySpan =
                        ObservabilityHelper.getSpanForRequest(
                                request,
                                "RetryHandler_Intercept - attempt " + executionCount,
                                span);
                retrySpan.setAttribute("http.retry_count", executionCount);
                retrySpan.setAttribute("http.status_code", response.code());
                retrySpan.end();
                response = chain.proceed(request);
                if (response == null)
                    throw new RuntimeException("unable to get a response from the chain");
            }
            return response;
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
