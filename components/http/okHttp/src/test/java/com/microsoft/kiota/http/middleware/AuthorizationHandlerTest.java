package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.KiotaClientFactory;

import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;

class AuthorizationHandlerTest {

    private static final String ACCESS_TOKEN_STRING = "token";
    private static final String TOKEN_AFTER_CAE = "TOKEN_AFTER_CAE";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PREV_AUTHORIZATION_HEADER_VALUE = "Bearer 123";
    private static final String NEW_AUTHORIZATION_HEADER_VALUE = "Bearer " + ACCESS_TOKEN_STRING;
    private static final String CLAIMS_CHALLENGE_HEADER_VALUE =
            "Bearer  authorization_uri=\"https://login.windows.net/common/oauth2/authorize\","
                + "error=\"insufficient_claims\","
                + "claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwNDEwNjY1MSJ9fX0=\"";

    @Test
    void testDoesNotAddAuthorizationHeaderIfAlreadyPresent() throws IOException {
        final Request request =
                new Request.Builder()
                        .url("https://graph.microsoft.com/v1.0/me")
                        .addHeader("Authorization", "Bearer 123")
                        .build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler =
                new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                PREV_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testAddsAuthorizationHeaderIfNotPresent() throws IOException {
        final Request request =
                new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler =
                new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                NEW_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testAddsAuthHeaderOnlyToAllowedHosts() throws IOException {
        final Request request =
                new Request.Builder().url("https://canary.graph.microsoft.com/v1.0/me").build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(!response.request().headers().names().contains(AUTHORIZATION_HEADER));
    }

    @Test
    void testAttemptsCAEChallenge() throws IOException {
        final Request request =
                new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        final Chain mockChain =
                getMockChain(request, getMockResponseWithClaimsChallengeHeader(request));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals("Bearer " + TOKEN_AFTER_CAE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testOtherRequestPropertiesAreNotAltered() throws IOException {
        final Request request =
                new Request.Builder()
                        .url("https://graph.microsoft.com/v1.0/me")
                        .addHeader("content-type", "application/json")
                        .get()
                        .build();
        final Chain mockChain = getMockChain(request, mock(Response.class));
        final AuthorizationHandler handler =
                new AuthorizationHandler(getMockAuthenticationProvider());
        Response response = handler.intercept(mockChain);

        assertEquals(request.url(), response.request().url());
        assertEquals(request.method(), response.request().method());
        assertTrue(response.request().headers().names().contains("content-type"));
        assertEquals("application/json", response.request().header("content-type"));
        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                NEW_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testDoesNotRetryCAEChallengeForOneShotBodyRequests() throws IOException {
        final RequestBody mockRequestBody = mock(RequestBody.class);
        when(mockRequestBody.isOneShot()).thenReturn(true);
        final Request request =
                new Request.Builder()
                        .url("https://graph.microsoft.com/v1.0/me")
                        .post(mockRequestBody)
                        .build();
        final Chain mockChain =
                getMockChain(request, getMockResponseWithClaimsChallengeHeader(request));
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                NEW_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testDoesNotAttemptCAEChallengeIfNoClaimsPresent() throws IOException {
        final Request request =
                new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);
        final Chain mockChain = getMockChain(request, mockResponse);
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        final AuthorizationHandler handler = new AuthorizationHandler(authProvider);
        Response response = handler.intercept(mockChain);

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                NEW_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
        assertEquals(401, response.code());
    }

    @Test
    void testAuthorizationHandlerAddedByClientFactory() throws IOException {
        final BaseBearerTokenAuthenticationProvider authProvider = getMockAuthenticationProvider();
        OkHttpClient okHttpClient =
                KiotaClientFactory.create(authProvider)
                        .addInterceptor(new MockResponseHandler())
                        .build();

        final Request request =
                new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
        Response response = okHttpClient.newCall(request).execute();

        assertTrue(response.request().headers().names().contains(AUTHORIZATION_HEADER));
        assertEquals(
                NEW_AUTHORIZATION_HEADER_VALUE, response.request().header(AUTHORIZATION_HEADER));
    }

    @Test
    void testSameOriginClaimsChallengeReplaysOnlyTheRedirectedRequest() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(
                    new MockResponse().setResponseCode(303).setHeader("Location", "/target"));
            server.enqueue(
                    new MockResponse()
                            .setResponseCode(401)
                            .setHeader("WWW-Authenticate", CLAIMS_CHALLENGE_HEADER_VALUE));
            server.enqueue(new MockResponse());
            final OkHttpClient client =
                    KiotaClientFactory.create(getMockAuthenticationProvider(server.url("/").host()))
                            .build();
            final Request request =
                    new Request.Builder()
                            .url(server.url("/initial"))
                            .post(RequestBody.create("test", null))
                            .build();

            try (Response response = client.newCall(request).execute()) {
                assertEquals(200, response.code());
            }
            assertEquals(3, server.getRequestCount());
            assertEquals("/initial", server.takeRequest().getPath());
            final RecordedRequest firstHopToTarget = server.takeRequest();
            assertEquals("/target", firstHopToTarget.getPath());
            assertEquals(
                    NEW_AUTHORIZATION_HEADER_VALUE,
                    firstHopToTarget.getHeader(AUTHORIZATION_HEADER));
            final RecordedRequest replay = server.takeRequest();
            assertEquals("/target", replay.getPath());
            assertEquals("GET", replay.getMethod());
            assertEquals(
                    Arrays.asList("Bearer " + TOKEN_AFTER_CAE),
                    replay.getHeaders().values(AUTHORIZATION_HEADER));
        }
    }

    @Test
    void testDifferentOriginDoesNotReceiveAuthorizationOrTriggerClaimsRetry() throws Exception {
        try (MockWebServer initial = new MockWebServer();
                MockWebServer target = new MockWebServer()) {
            initial.enqueue(
                    new MockResponse()
                            .setResponseCode(302)
                            .setHeader("Location", target.url("/target")));
            initial.enqueue(new MockResponse());
            target.enqueue(
                    new MockResponse()
                            .setResponseCode(401)
                            .setHeader("WWW-Authenticate", CLAIMS_CHALLENGE_HEADER_VALUE));
            target.enqueue(new MockResponse());
            final BaseBearerTokenAuthenticationProvider provider =
                    getMockAuthenticationProvider(initial.url("/").host());
            final OkHttpClient client = KiotaClientFactory.create(provider).build();
            final Request request = new Request.Builder().url(initial.url("/initial")).build();

            try (Response response = client.newCall(request).execute()) {
                assertEquals(401, response.code());
            }
            assertEquals(1, initial.getRequestCount());
            assertEquals(
                    NEW_AUTHORIZATION_HEADER_VALUE,
                    initial.takeRequest().getHeader(AUTHORIZATION_HEADER));
            assertEquals(1, target.getRequestCount());
            assertNull(target.takeRequest().getHeader(AUTHORIZATION_HEADER));
            verify(provider.getAccessTokenProvider(), times(1))
                    .getAuthorizationToken(any(URI.class), anyMap());
        }
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
        return getMockAuthenticationProvider("graph.microsoft.com");
    }

    private BaseBearerTokenAuthenticationProvider getMockAuthenticationProvider(
            String allowedHost) {
        final AccessTokenProvider mockAccessTokenProvider = mock(AccessTokenProvider.class);
        final AllowedHostsValidator allowedHostsValidator = new AllowedHostsValidator(allowedHost);
        when(mockAccessTokenProvider.getAllowedHostsValidator()).thenReturn(allowedHostsValidator);
        when(mockAccessTokenProvider.getAuthorizationToken(any(URI.class), anyMap()))
                .thenReturn(ACCESS_TOKEN_STRING, TOKEN_AFTER_CAE);
        final BaseBearerTokenAuthenticationProvider mockAuthenticationProvider =
                mock(BaseBearerTokenAuthenticationProvider.class);
        when(mockAuthenticationProvider.getAccessTokenProvider())
                .thenReturn(mockAccessTokenProvider);
        return mockAuthenticationProvider;
    }

    private Response getMockResponseWithClaimsChallengeHeader(Request request) {
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);
        when(mockResponse.headers("WWW-Authenticate"))
                .thenReturn(Arrays.asList(CLAIMS_CHALLENGE_HEADER_VALUE));
        when(mockResponse.request()).thenReturn(request);
        return mockResponse;
    }
}
