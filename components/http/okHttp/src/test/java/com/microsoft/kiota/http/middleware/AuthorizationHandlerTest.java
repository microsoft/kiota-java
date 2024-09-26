package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;

import okhttp3.Response;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AuthorizationHandlerTest {

    final String token = "token";
    final String tokenAfterCAE = "tokenAfterCAE";
    final String authHeader = "Authorization";
    final String prevAuthHeaderValue = "Bearer 123";
    final String newAuthHeaderValue = "Bearer " + token;
    final String claimsChallengeHeaderValue = "Bearer" +
                "  authorization_uri=\"https://login.windows.net/common/oauth2/authorize\"," +
                "error=\"insufficient_claims\"," +
                "claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwNDEwNjY1MSJ9fX0=\"";

    @Test
    void testDoesNotAddAuthorizationHeaderIfAlreadyPresent() throws IOException {
        final Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").addHeader("Authorization", "Bearer 123").build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler = new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(authHeader));
        assertEquals(prevAuthHeaderValue, response.request().header(authHeader));
    }

    @Test
    void testAddsAuthorizationHeaderIfNotPresent() throws IOException {
        final Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler = new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(authHeader));
        assertEquals(newAuthHeaderValue, response.request().header(authHeader));
    }

    @Test
    void testAddsAuthHeaderOnlyToAllowedHosts() throws IOException {
        final Request request = new Request.Builder().url("https://canary.graph.microsoft.com/v1.0/me").build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(!response.request().headers().names().contains(authHeader));
    }

    @Test
    void testAttemptsCAEChallenge() throws IOException {
        final Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        final Chain mockChain = getMockChain(request, getMockResponseWithClaimsChallengeHeader(request));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(authHeader));
        assertEquals("Bearer " + tokenAfterCAE, response.request().header(authHeader));
    }

    @Test
    void testOtherRequestPropertiesAreNotAltered() throws IOException {
        final Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").addHeader("content-type", "application/json").get().build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler = new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertEquals(request.url(), response.request().url());
        assertEquals(request.method(), response.request().method());
        assertTrue(response.request().headers().names().contains("content-type"));
        assertEquals("application/json", response.request().header("content-type"));
        assertTrue(response.request().headers().names().contains(authHeader));
        assertEquals(newAuthHeaderValue, response.request().header(authHeader));
    }

    @Test
    void testDoesNotRetryCAEChallengeForOneShotBodyRequests() throws IOException {
        final RequestBody mockRequestBody = mock(RequestBody.class);
        when(mockRequestBody.isOneShot()).thenReturn(true);
        final Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").post(mockRequestBody).build();
        final Chain mockChain = getMockChain(request, getMockResponseWithClaimsChallengeHeader(request));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(authHeader));
        assertEquals(newAuthHeaderValue, response.request().header(authHeader));
    }

    private Chain getMockChain(Request mockRequest, Response mockResponse) throws IOException {
        Chain mockChain = mock(Chain.class);
        when(mockChain.request()).thenReturn(mockRequest);
        when(mockChain.proceed(any(Request.class)))
            .thenAnswer(
                new Answer<Response>() {
                    public Response answer(InvocationOnMock invocation) {
                        Object[] args = invocation.getArguments();
                        Request request = (Request) args[0];
                        when(mockResponse.request()).thenReturn(request);
                        return mockResponse;
                    }
                });
        return mockChain;
    }

    private BaseBearerTokenAuthenticationProvider getMockAuthenticationProvider() {
        final AccessTokenProvider mockAccessTokenProvider = mock(AccessTokenProvider.class);
        final AllowedHostsValidator allowedHostsValidator = new AllowedHostsValidator("graph.microsoft.com");
        when(mockAccessTokenProvider.getAllowedHostsValidator()).thenReturn(allowedHostsValidator);
        when(mockAccessTokenProvider.getAuthorizationToken(any(URI.class), anyMap())).thenReturn(token, tokenAfterCAE);
        final BaseBearerTokenAuthenticationProvider mockAuthenticationProvider = mock(BaseBearerTokenAuthenticationProvider.class);
        when(mockAuthenticationProvider.getAccessTokenProvider()).thenReturn(mockAccessTokenProvider);
        return mockAuthenticationProvider;
    }

    private Response getMockResponseWithClaimsChallengeHeader(Request request) {
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);
        when(mockResponse.headers("WWW-Authenticate")).thenReturn(Arrays.asList(claimsChallengeHeaderValue));
        when(mockResponse.request()).thenReturn(request);
        return mockResponse;
    }
}
