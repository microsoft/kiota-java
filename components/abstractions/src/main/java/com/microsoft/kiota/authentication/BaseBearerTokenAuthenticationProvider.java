package com.microsoft.kiota.authentication;

import com.microsoft.kiota.Compatibility;
import com.microsoft.kiota.RequestInformation;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/** Provides a base class for implementing AuthenticationProvider for Bearer token scheme. */
public class BaseBearerTokenAuthenticationProvider implements AuthenticationProvider {
    /**
     * Instantiates a new BaseBearerTokenAuthenticationProvider.
     * @param accessTokenProvider the access token provider.
     */
    public BaseBearerTokenAuthenticationProvider(
            @Nonnull final AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = Objects.requireNonNull(accessTokenProvider);
    }

    private final AccessTokenProvider accessTokenProvider;
    private static final String authorizationHeaderKey = "Authorization";
    private static final String ClaimsKey = "claims";

    public void authenticateRequest(
            @Nonnull final RequestInformation request,
            @Nullable final Map<String, Object> additionalAuthenticationContext) {
        Objects.requireNonNull(request);
        if (request.headers.containsKey(authorizationHeaderKey)
                && additionalAuthenticationContext != null
                && additionalAuthenticationContext.containsKey(ClaimsKey)) {
            request.headers.remove(authorizationHeaderKey);
        }
        if (!request.headers.containsKey(authorizationHeaderKey)) {
            final URI targetUri;
            try {
                targetUri = request.getUri();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Malformed URI.", e);
            }
            String accessToken =
                    this.accessTokenProvider.getAuthorizationToken(
                            targetUri, additionalAuthenticationContext);
            if (!Compatibility.isBlank(accessToken)) {
                request.headers.add(authorizationHeaderKey, "Bearer " + accessToken);
            }
        }
    }

    public @Nonnull AccessTokenProvider getAccessTokenProvider() {
        return this.accessTokenProvider;
    }
}
