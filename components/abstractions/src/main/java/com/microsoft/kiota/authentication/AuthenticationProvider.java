package com.microsoft.kiota.authentication;

import com.microsoft.kiota.RequestInformation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Map;

/** Authenticates the application request. */
public interface AuthenticationProvider {
    /**
     * Authenticates the application request.
     * @param request the request to authenticate.
     * @param additionalAuthenticationContext Additional authentication context to pass to the authentication library.
     */
    void authenticateRequest(
            @Nonnull final RequestInformation request,
            @Nullable final Map<String, Object> additionalAuthenticationContext);
}
