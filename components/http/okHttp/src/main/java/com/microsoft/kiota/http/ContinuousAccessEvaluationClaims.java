package com.microsoft.kiota.http;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContinuousAccessEvaluationClaims {

    private static final Pattern bearerPattern =
            Pattern.compile("^Bearer\\s.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern claimsPattern =
            Pattern.compile("\\s?claims=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private static final String wwwAuthenticateHeader = "WWW-Authenticate";

    public static @Nullable String getClaimsFromResponse(@Nonnull Response response) {
        if (response == null || response.code() != 401) {
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
