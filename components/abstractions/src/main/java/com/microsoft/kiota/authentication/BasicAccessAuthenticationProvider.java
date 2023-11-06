package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/** Provides an implementation of the Basic Access Authentication scheme: https://en.wikipedia.org/wiki/Basic_access_authentication . */
public class BasicAccessAuthenticationProvider implements AuthenticationProvider {
    private final static String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String BASIC = "Basic ";

    private final String username;
    private final String password;
    private final String encoded;

    /**
     * Instantiates a new BasicAccessAuthenticationProvider.
     * @param username the username to be used.
     * @param password the password to be used.
     */
    public BasicAccessAuthenticationProvider(@Nonnull final String username, @Nonnull final String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        this.username = username;
        this.password = password;
        encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    /** {@inheritDoc} */
    @Override
    public void authenticateRequest(@Nonnull final RequestInformation request, @Nullable final Map<String, Object> additionalAuthenticationContext) {
        request.headers.add(AUTHORIZATION_HEADER_KEY, BASIC + encoded);
    }
}
