package com.microsoft.kiota.http;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Response;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to extract the claims from the WWW-Authenticate header in a response.
 * https://learn.microsoft.com/en-us/entra/identity/conditional-access/concept-continuous-access-evaluation
 */
public final class ContinuousAccessEvaluationClaims {

    private static final Pattern bearerPattern =
            Pattern.compile("^Bearer\\s.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern claimsPattern =
            Pattern.compile("\\s?claims=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private static final String wwwAuthenticateHeader = "WWW-Authenticate";

    /**
     * Extracts the claims from the WWW-Authenticate header in a response.
     * @param response the response to extract the claims from.
     * @return the claims
     */
    public static @Nullable String getClaimsFromResponse(@Nonnull Response response) {
        Objects.requireNonNull(response, "parameter response cannot be null");
        if (response.code() != 401) {
            return null;
        }
        final List<String> authenticateHeader = response.headers(wwwAuthenticateHeader);
        if (!authenticateHeader.isEmpty()) {
            String rawHeaderValue = null;
            for (final String authenticateEntry : authenticateHeader) {
                final Matcher matcher = bearerPattern.matcher(authenticateEntry);
                if (matcher.matches()) {
                    rawHeaderValue = authenticateEntry.replaceFirst("^Bearer\\s", "");
                    break;
                }
            }
            if (rawHeaderValue != null) {
                final String[] parameters = rawHeaderValue.split(",");
                for (final String parameter : parameters) {
                    final Matcher matcher = claimsPattern.matcher(parameter);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        }
        return null;
    }
}
