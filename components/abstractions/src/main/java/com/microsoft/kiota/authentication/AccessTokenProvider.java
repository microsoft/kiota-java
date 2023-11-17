package com.microsoft.kiota.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.util.Map;

/** Returns access tokens */
public interface AccessTokenProvider {
    /**
     * This method returns the access token for the provided url.
     * @param uri The target URI to get an access token for.
     * @param additionalAuthenticationContext Additional authentication context to pass to the authentication library.
     * @return the access token.
     */
    @Nonnull String getAuthorizationToken(
            @Nonnull final URI uri,
            @Nullable final Map<String, Object> additionalAuthenticationContext);

    /**
     * Returns the allowed hosts validator.
     * @return The allowed hosts validator.
     */
    @Nonnull AllowedHostsValidator getAllowedHostsValidator();
}
