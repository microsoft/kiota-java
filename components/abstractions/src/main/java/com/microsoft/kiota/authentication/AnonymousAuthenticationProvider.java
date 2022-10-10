package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This authentication provider does not perform any authentication. */
public class AnonymousAuthenticationProvider implements AuthenticationProvider {
    @Nonnull
    public CompletableFuture<Void> authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext) {
        return CompletableFuture.completedFuture(null);
    }    
}