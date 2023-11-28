package com.microsoft.kiota.http.middleware;

import static okhttp3.internal.http.StatusLine.HTTP_PERM_REDIRECT;
import static okhttp3.internal.http.StatusLine.HTTP_TEMP_REDIRECT;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

import com.microsoft.kiota.http.middleware.options.RedirectHandlerOption;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.Objects;

/**
 * Middleware that determines whether a redirect information should be followed or not, and follows it if necessary.
 */
public class RedirectHandler implements Interceptor {
    @Nonnull private final RedirectHandlerOption mRedirectOption;

    /**
     * Initialize using default redirect options, default IShouldRedirect and max redirect value
     */
    public RedirectHandler() {
        this(null);
    }

    /**
     * Initialize using custom redirect options.
     * @param redirectOption pass instance of redirect options to be used
     */
    public RedirectHandler(@Nullable final RedirectHandlerOption redirectOption) {
        if (redirectOption == null) {
            this.mRedirectOption = new RedirectHandlerOption();
        } else {
            this.mRedirectOption = redirectOption;
        }
    }

    boolean isRedirected(
            @Nonnull final Request request,
            @Nonnull final Response response,
            int redirectCount,
            @Nonnull final RedirectHandlerOption redirectOption)
            throws IOException {
        Objects.requireNonNull(request, "parameter request cannot be null");
        Objects.requireNonNull(response, "parameter response cannot be null");
        Objects.requireNonNull(redirectOption, "parameter redirectOption cannot be null");
        // Check max count of redirects reached
        if (redirectCount > redirectOption.maxRedirects()) return false;

        // Location header empty then don't redirect
        final String locationHeader = response.header("location");
        if (locationHeader == null) return false;

        // If any of 301,302,303,307,308 then redirect
        final int statusCode = response.code();
        if (statusCode == HTTP_PERM_REDIRECT
                || // 308
                statusCode == HTTP_MOVED_PERM
                || // 301
                statusCode == HTTP_TEMP_REDIRECT
                || // 307
                statusCode == HTTP_SEE_OTHER
                || // 303
                statusCode == HTTP_MOVED_TEMP) // 302
        return true;

        return false;
    }

    Request getRedirect(final Request request, final Response userResponse)
            throws ProtocolException {
        String location = userResponse.header("Location");
        if (location == null || location.length() == 0) return null;

        // For relative URL in location header, the new url to redirect is relative to original
        // request
        if (location.startsWith("/")) {
            if (request.url().toString().endsWith("/")) {
                location = location.substring(1);
            }
            location = request.url() + location;
        }

        HttpUrl requestUrl = userResponse.request().url();

        HttpUrl locationUrl = userResponse.request().url().resolve(location);

        // Don't follow redirects to unsupported protocols.
        if (locationUrl == null) return null;

        // Most redirects don't include a request body.
        Request.Builder requestBuilder = userResponse.request().newBuilder();

        // When redirecting across hosts, drop all authentication headers. This
        // is potentially annoying to the application layer since they have no
        // way to retain them.
        boolean sameScheme = locationUrl.scheme().equalsIgnoreCase(requestUrl.scheme());
        boolean sameHost =
                locationUrl.host().toString().equalsIgnoreCase(requestUrl.host().toString());
        if (!sameScheme || !sameHost) {
            requestBuilder.removeHeader("Authorization");
        }

        // Response status code 303 See Other then POST changes to GET
        if (userResponse.code() == HTTP_SEE_OTHER) {
            requestBuilder.method("GET", null);
        }

        return requestBuilder.url(locationUrl).build();
    }

    // Intercept request and response made to network
    @Override
    @SuppressWarnings("UnknownNullness")
    @Nonnull public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        Request request = chain.request();
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        Response response = null;
        int requestsCount = 1;
        boolean shouldRedirect = true;

        // Use should retry pass along with this request
        RedirectHandlerOption redirectOption = request.tag(RedirectHandlerOption.class);
        redirectOption = redirectOption != null ? redirectOption : this.mRedirectOption;

        final Span span =
                ObservabilityHelper.getSpanForRequest(request, "RedirectHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.redirect.enable", true);
        }
        try {
            do {
                if (span != null) {
                    request = request.newBuilder().tag(Span.class, span).build();
                    if (request == null) {
                        throw new IllegalArgumentException("request cannot be null");
                    }
                }
                response = chain.proceed(request);
                if (response == null) {
                    throw new IllegalArgumentException("response cannot be null");
                }
                shouldRedirect =
                        isRedirected(request, response, requestsCount, redirectOption)
                                && redirectOption.shouldRedirect().shouldRedirect(response);

                final Request followup = shouldRedirect ? getRedirect(request, response) : null;
                if (followup != null) {
                    response.close();
                    request = followup;
                    requestsCount++;
                    final Span redirectSpan =
                            ObservabilityHelper.getSpanForRequest(
                                    request,
                                    "RedirectHandler_Intercept - redirect " + requestsCount,
                                    span);
                    redirectSpan.setAttribute(
                            "com.microsoft.kiota.handler.redirect.count", requestsCount);
                    redirectSpan.setAttribute("http.status_code", response.code());
                    redirectSpan.end();
                }
            } while (shouldRedirect);
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
        return response;
    }
}
