package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

class AzureIdentityAccessTokenProviderTest {

    @ParameterizedTest
    @ValueSource(
            strings = {"http://localhost:80/me", "http://127.0.0.1/me", "http://[::1]:8080/me"})
    void testLocalhostHttpUrlIsValid(String urlString) throws URISyntaxException {
        var tokenCredential = mock(TokenCredential.class);
        when(tokenCredential.getTokenSync(any(TokenRequestContext.class)))
                .thenReturn(new AccessToken("token", null));
        var accessTokenProvider = new AzureIdentityAccessTokenProvider(tokenCredential, null, "");
        assertEquals(
                "token",
                accessTokenProvider.getAuthorizationToken(new URI(urlString), new HashMap<>()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://graph.microsoft.com/me"})
    void testNonLocalhostHttpUrlIsInvalid(String urlString) throws URISyntaxException {
        var tokenCredential = mock(TokenCredential.class);
        var accessTokenProvider = new AzureIdentityAccessTokenProvider(tokenCredential, null, "");
        final var uri = new URI(urlString);
        assertThrows(
                IllegalArgumentException.class,
                () -> accessTokenProvider.getAuthorizationToken(uri, new HashMap<>()));
    }
}
