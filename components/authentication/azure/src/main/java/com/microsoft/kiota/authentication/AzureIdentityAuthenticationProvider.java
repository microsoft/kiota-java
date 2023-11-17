package com.microsoft.kiota.authentication;

import com.azure.core.credential.TokenCredential;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Implementation of AuthenticationProvider that supports implementations of TokenCredential from Azure.Identity. */
public class AzureIdentityAuthenticationProvider extends BaseBearerTokenAuthenticationProvider {
    /**
     * Creates a new instance of AzureIdentityAuthenticationProvider.
     * @param tokenCredential The Azure.Identity.TokenCredential implementation to use.
     * @param allowedHosts The list of allowed hosts for which to request access tokens.
     * @param scopes The scopes to request access tokens for.
     */
    @SuppressWarnings("LambdaLast") public AzureIdentityAuthenticationProvider(
            @Nonnull final TokenCredential tokenCredential,
            @Nonnull final String[] allowedHosts,
            @Nonnull final String... scopes) {
        this(tokenCredential, allowedHosts, null, scopes);
    }

    /**
     * Creates a new instance of AzureIdentityAuthenticationProvider.
     * @param tokenCredential The Azure.Identity.TokenCredential implementation to use.
     * @param allowedHosts The list of allowed hosts for which to request access tokens.
     * @param observabilityOptions The observability options to use.
     * @param scopes The scopes to request access tokens for.
     */
    @SuppressWarnings("LambdaLast") public AzureIdentityAuthenticationProvider(
            @Nonnull final TokenCredential tokenCredential,
            @Nonnull final String[] allowedHosts,
            @Nullable final ObservabilityOptions observabilityOptions,
            @Nonnull final String... scopes) {
        super(
                new AzureIdentityAccessTokenProvider(
                        tokenCredential, allowedHosts, observabilityOptions, scopes));
    }
}
