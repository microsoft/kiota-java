package com.microsoft.kiota.authentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import com.microsoft.kiota.RequestInformation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/** This authentication provider adds an API key to the request as a query parameter or header. */
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final ApiKeyLocation location;
    @Nonnull
    private final String paramName;
    @Nonnull
    private final String apiKey;
    private final AllowedHostsValidator validator;
    /**
     * Creates a new instance of ApiKeyAuthenticationProvider.
     * @param apiKey The API key to use.
     * @param paramName The name of the query parameter or header to use.
     * @param location The location of the API key.
     * @param validHosts The list of allowed hosts for which to add the API key.
     */
    public ApiKeyAuthenticationProvider(@Nonnull final String apiKey, @Nonnull final String paramName, @Nonnull final ApiKeyLocation location, @Nonnull final String ...validHosts) {
        Objects.requireNonNull(apiKey);
        Objects.requireNonNull(paramName);
        Objects.requireNonNull(location);
        if (apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be empty");
        }
        if (paramName.isEmpty()) {
            throw new IllegalArgumentException("paramName cannot be empty");
        }
        this.apiKey = apiKey;
        this.paramName = paramName;
        this.location = location;
        this.validator = new AllowedHostsValidator(validHosts);
    }
    private static final String parentSpanKey = "parent-span";
    /** {@inheritDoc} */
    @Override
    public void authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext) throws URISyntaxException {
        Objects.requireNonNull(request);
        Span span;
        if(additionalAuthenticationContext != null && additionalAuthenticationContext.containsKey(parentSpanKey) && additionalAuthenticationContext.get(parentSpanKey) instanceof Span) {
            final Span parentSpan = (Span) additionalAuthenticationContext.get(parentSpanKey);
            span = GlobalOpenTelemetry.getTracer("com.microsoft.kiota").spanBuilder("authenticateRequest").setParent(Context.current().with(parentSpan)).startSpan();
        } else {
            span = GlobalOpenTelemetry.getTracer("com.microsoft.kiota").spanBuilder("authenticateRequest").startSpan();
        }
        try(final Scope scope = span.makeCurrent()) {
            final URI uri = request.getUri();
            if(uri == null || !validator.isUrlHostValid(uri)) {
                span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", false);
                return;
            }
            if(!uri.getScheme().equalsIgnoreCase("https")) {
                span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", false);
                throw new IllegalArgumentException("Only https is supported");
            }
            span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", true);

            switch(location) {
                case HEADER:
                    request.headers.add(paramName, apiKey);
                    break;
                case QUERY_PARAMETER:
                    request.setUri(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery() == null ? paramName + "=" + apiKey : uri.getQuery() + "&" + paramName + "=" + apiKey, uri.getFragment()));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported key location");
            }
        } catch (URISyntaxException | IllegalArgumentException e ) {
            span.recordException(e);
            throw e;
        }
        finally {
            span.end();
        }
    }
}
