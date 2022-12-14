package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Provides a base class for implementing AuthenticationProvider for Bearer token scheme. */
public class BaseBearerTokenAuthenticationProvider implements AuthenticationProvider {
    public BaseBearerTokenAuthenticationProvider(@Nonnull final AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = Objects.requireNonNull(accessTokenProvider);
    }
    private final AccessTokenProvider accessTokenProvider;
    private final static String authorizationHeaderKey = "Authorization";
    private final static String ClaimsKey = "claims";
    @Nonnull
    public CompletableFuture<Void> authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext) {
        Objects.requireNonNull(request);

        if (request.headers.containsKey(authorizationHeaderKey) &&
            additionalAuthenticationContext != null &&
            additionalAuthenticationContext.containsKey(ClaimsKey))
        {
            request.headers.remove(authorizationHeaderKey);
        }
        if(!request.headers.containsKey(authorizationHeaderKey)) {
            final URI targetUri;
            try {
                targetUri = request.getUri();
            } catch (URISyntaxException e) {
                final CompletableFuture<Void> result = new CompletableFuture<>();
                result.completeExceptionally(e);
                return result;
            }
            return this.accessTokenProvider.getAuthorizationToken(targetUri, additionalAuthenticationContext)
                .thenApply(token -> {
                    if(token != null && !token.isEmpty()) { 
                    // Not an error, just no need to authenticate as we might have been given an external URL from the main API (large file upload, etc.)
                        request.headers.add(authorizationHeaderKey, "Bearer " + token);
                    }
                    return null;
                });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}