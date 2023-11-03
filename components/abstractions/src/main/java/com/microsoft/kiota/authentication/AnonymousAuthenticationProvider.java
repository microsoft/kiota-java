package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;

import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** This authentication provider does not perform any authentication. */
public class AnonymousAuthenticationProvider implements AuthenticationProvider {
    /** Default constructor for the anonymous authentication provider. */
    public AnonymousAuthenticationProvider() {}
    /** {@inheritDoc} */
    public void authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext) {
        //This authentication provider does not perform any authentication.
    }
}