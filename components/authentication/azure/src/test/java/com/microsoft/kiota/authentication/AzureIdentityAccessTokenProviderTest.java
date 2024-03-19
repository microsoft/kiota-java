package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AzureIdentityAccessTokenProviderTest {

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
    void testNonLocalhostHttpUrlIsInvalid(String urlString) {
        var tokenCredential = mock(TokenCredential.class);
        var accessTokenProvider = new AzureIdentityAccessTokenProvider(tokenCredential, null, "");
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        accessTokenProvider.getAuthorizationToken(
                                new URI(urlString), new HashMap<>()));
    }

    @Test
    void testKeepUserProvidedScopes() throws URISyntaxException {
        var tokenCredential = mock(TokenCredential.class);
        String[] userProvidedScopes = {
            "https://graph.microsoft.com/User.Read", "https://graph.microsoft.com/Application.Read"
        };
        var accessTokenProvider =
                new AzureIdentityAccessTokenProvider(
                        tokenCredential, new String[] {}, userProvidedScopes);
        assertScopes(tokenCredential, accessTokenProvider, userProvidedScopes);
    }

    @Test
    void testConfigureDefaultScopeWhenScopesNotProvided() throws URISyntaxException {
        var tokenCredential = mock(TokenCredential.class);
        var accessTokenProvider =
                new AzureIdentityAccessTokenProvider(tokenCredential, new String[] {});
        assertScopes(
                tokenCredential,
                accessTokenProvider,
                new String[] {"https://graph.microsoft.com/.default"});
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testConfigureDefaultScopeWhenScopesNullOrEmpty(String[] nullOrEmptyUserProvidedScopes)
            throws URISyntaxException {
        var tokenCredential = mock(TokenCredential.class);
        var accessTokenProvider =
                new AzureIdentityAccessTokenProvider(
                        tokenCredential, new String[] {}, nullOrEmptyUserProvidedScopes);
        assertScopes(
                tokenCredential,
                accessTokenProvider,
                new String[] {"https://graph.microsoft.com/.default"});
    }

    private static void assertScopes(
            TokenCredential tokenCredential,
            AzureIdentityAccessTokenProvider accessTokenProvider,
            String[] expectedScopes)
            throws URISyntaxException {
        var tokenRequestContextArgumentCaptor = ArgumentCaptor.forClass(TokenRequestContext.class);
        when(tokenCredential.getTokenSync(tokenRequestContextArgumentCaptor.capture()))
                .thenReturn(mock(AccessToken.class));

        accessTokenProvider.getAuthorizationToken(
                new URI("https://graph.microsoft.com"), new HashMap<>());

        List<String> actualScopes = tokenRequestContextArgumentCaptor.getValue().getScopes();
        assertLinesMatch(actualScopes, Arrays.asList(expectedScopes));
    }
}
