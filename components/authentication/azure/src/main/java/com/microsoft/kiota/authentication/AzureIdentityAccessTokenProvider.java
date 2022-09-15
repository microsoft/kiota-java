package com.microsoft.kiota.authentication;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import com.microsoft.kiota.authentication.AccessTokenProvider;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;

/** Implementation of AccessTokenProvider that supports implementations of TokenCredential from Azure.Identity. */
public class AzureIdentityAccessTokenProvider implements AccessTokenProvider {
    private final TokenCredential creds;
    private final List<String> _scopes;
    private final AllowedHostsValidator _hostValidator;
    private final ObservabilityOptions _observabilityOptions;
    /**
     * Creates a new instance of AzureIdentityAccessTokenProvider.
     * @param tokenCredential The Azure.Identity.TokenCredential implementation to use.
     * @param allowedHosts The list of allowed hosts for which to request access tokens.
     * @param scopes The scopes to request access tokens for.
     */
    public AzureIdentityAccessTokenProvider(@Nonnull final TokenCredential tokenCredential, @Nonnull final String[] allowedHosts, @Nonnull final String... scopes) {
        this(tokenCredential, allowedHosts, null, scopes);
    }
    /**
     * Creates a new instance of AzureIdentityAccessTokenProvider.
     * @param tokenCredential The Azure.Identity.TokenCredential implementation to use.
     * @param allowedHosts The list of allowed hosts for which to request access tokens.
     * @param observabilityOptions The observability options to use.
     * @param scopes The scopes to request access tokens for.
     */
    public AzureIdentityAccessTokenProvider(@Nonnull final TokenCredential tokenCredential, @Nonnull final String[] allowedHosts, @Nullable final ObservabilityOptions observabilityOptions,  @Nonnull final String... scopes) {
        creds = Objects.requireNonNull(tokenCredential, "parameter tokenCredential cannot be null");

        if(scopes == null) {
            _scopes = new ArrayList<String>();
        } else if(scopes.length == 0) {
            _scopes = Arrays.asList(new String[] { "https://graph.microsoft.com/.default" });
        } else {
            _scopes = Arrays.asList(scopes);
        }
        if (allowedHosts == null || allowedHosts.length == 0) {
            _hostValidator = new AllowedHostsValidator(new String[] { "graph.microsoft.com", "graph.microsoft.us", "dod-graph.microsoft.us", "graph.microsoft.de", "microsoftgraph.chinacloudapi.cn", "canary.graph.microsoft.com" });
        } else {
            _hostValidator = new AllowedHostsValidator(allowedHosts);
        }
        if (observabilityOptions == null) {
            _observabilityOptions = new ObservabilityOptions();
        } else {
            _observabilityOptions = observabilityOptions;
        }
    }
    private final static String ClaimsKey = "claims";
    private final static String parentSpanKey = "parent-span";
    @Nonnull
    public CompletableFuture<String> getAuthorizationToken(@Nonnull final URI uri, @Nullable final Map<String, Object> additionalAuthenticationContext) {
        Span span;
        if(additionalAuthenticationContext != null && additionalAuthenticationContext.containsKey(parentSpanKey) && additionalAuthenticationContext.get(parentSpanKey) instanceof Span) {
            final Span parentSpan = (Span) additionalAuthenticationContext.get(parentSpanKey);
            span = GlobalOpenTelemetry.getTracer(_observabilityOptions.GetTracerInstrumentationName()).spanBuilder("getAuthorizationToken").setParent(Context.current().with(parentSpan)).startSpan();
        } else {
            span = GlobalOpenTelemetry.getTracer(_observabilityOptions.GetTracerInstrumentationName()).spanBuilder("getAuthorizationToken").startSpan();
        }
        try(final Scope scope = span.makeCurrent()) {
            if(!_hostValidator.isUrlHostValid(uri)) {
                span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", false);
                return CompletableFuture.completedFuture("");
            }
            if(!uri.getScheme().equalsIgnoreCase("https")) {
                span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", false);
                final Exception result = new IllegalArgumentException("Only https is supported");
                span.recordException(result);
                return CompletableFuture.failedFuture(result);
            }
            span.setAttribute("com.microsoft.kiota.authentication.is_url_valid", true);

            String decodedClaim = null;

            if(additionalAuthenticationContext != null && additionalAuthenticationContext.containsKey(ClaimsKey) && additionalAuthenticationContext.get(ClaimsKey) instanceof String) {
                final String rawClaim = (String) additionalAuthenticationContext.get(ClaimsKey);
                decodedClaim = new String(Base64.getDecoder().decode(rawClaim));
            }
            span.setAttribute("com.microsoft.kiota.authentication.additional_claims_provided", decodedClaim != null && !decodedClaim.isEmpty());

            final TokenRequestContext context = new TokenRequestContext() {{
                this.setScopes(_scopes);
            }};
            span.setAttribute("com.microsoft.kiota.authentication.scopes", String.join("|", _scopes));
            if(decodedClaim != null && !decodedClaim.isEmpty()) {
                context.setClaims(decodedClaim);
            }
            return this.creds.getToken(context).toFuture().thenApply(r -> r.getToken());
        } finally {
            span.end();
        }
    }
    @Nonnull
    public AllowedHostsValidator getAllowedHostsValidator() {
        return _hostValidator;
    }
}
