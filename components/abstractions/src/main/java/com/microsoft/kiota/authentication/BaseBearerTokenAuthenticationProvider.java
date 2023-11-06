package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;

import com.google.common.base.Strings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Provides a base class for implementing AuthenticationProvider for Bearer token scheme. */
public class BaseBearerTokenAuthenticationProvider implements AuthenticationProvider {
    /**
     * Instantiates a new BaseBearerTokenAuthenticationProvider.
     * @param accessTokenProvider the access token provider.
     */
    public BaseBearerTokenAuthenticationProvider(@Nonnull final AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = Objects.requireNonNull(accessTokenProvider);
    }
    private final AccessTokenProvider accessTokenProvider;
    private final static String authorizationHeaderKey = "Authorization";
    private final static String ClaimsKey = "claims";
    public void authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext){
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
            } catch (URISyntaxException e){
                throw new RuntimeException("Malformed URI.", e);
            }
            String accessToken = this.accessTokenProvider.getAuthorizationToken(targetUri, additionalAuthenticationContext);
            if(!Strings.isNullOrEmpty(accessToken)) {
                request.headers.add(authorizationHeaderKey, "Bearer " + accessToken);
            }
        }
    }
}