package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.microsoft.kiota.RequestOption;
import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.http.middleware.AuthorizationHandler;
import com.microsoft.kiota.http.middleware.ChaosHandler;
import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.RedirectHandler;
import com.microsoft.kiota.http.middleware.RetryHandler;
import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.UserAgentHandler;
import com.microsoft.kiota.http.middleware.options.HeadersInspectionOption;
import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KiotaClientFactoryTest {

    @Test
    void testCreatesDefaultInterceptors() throws IOException {
        OkHttpClient client = KiotaClientFactory.create().build();
        assertEquals(
                Arrays.asList(
                        UrlReplaceHandler.class,
                        ParametersNameDecodingHandler.class,
                        UserAgentHandler.class,
                        RedirectHandler.class,
                        RetryHandler.class,
                        HeadersInspectionHandler.class),
                client.interceptors().stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @Test
    void testDefaultInterceptorsWhenPassedIn() throws IOException {
        OkHttpClient client =
                KiotaClientFactory.create(
                                new Interceptor[] {getDisabledRetryHandler(), new ChaosHandler()})
                        .build();
        List<Interceptor> interceptors = client.interceptors();
        assertNotNull(interceptors);
        assertEquals(2, interceptors.size());
        for (Interceptor interceptor : interceptors) {
            if (interceptor instanceof RetryHandler) {
                RetryHandlerOption handlerOption = ((RetryHandler) interceptor).getRetryOptions();
                assertEquals(0, handlerOption.delay());
                assertEquals(0, handlerOption.maxRetries());
            }

            assertTrue(
                    interceptor instanceof RetryHandler || interceptor instanceof ChaosHandler,
                    "Array should contain instances of RetryHandler and ChaosHandler");
        }
    }

    @Test
    void testDefaultInterceptorsWhenRequestOptionsPassedIn() throws IOException {
        RetryHandlerOption retryHandlerOption =
                new RetryHandlerOption((delay, executionCount, request, response) -> false, 0, 0);
        UrlReplaceHandlerOption urlReplaceHandlerOption =
                new UrlReplaceHandlerOption(new HashMap<>(), false);

        final ArrayList<RequestOption> options = new ArrayList<>();
        options.add(urlReplaceHandlerOption);
        options.add(retryHandlerOption);

        Interceptor[] interceptors =
                KiotaClientFactory.createDefaultInterceptors(options.toArray(new RequestOption[0]));
        OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        List<Interceptor> clientInterceptors = client.interceptors();
        assertNotNull(interceptors);
        assertEquals(6, clientInterceptors.size());
        for (Interceptor interceptor : clientInterceptors) {
            if (interceptor instanceof RetryHandler) {
                RetryHandlerOption handlerOption = ((RetryHandler) interceptor).getRetryOptions();
                assertEquals(0, handlerOption.delay());
                assertEquals(0, handlerOption.maxRetries());
            }

            if (interceptor instanceof UrlReplaceHandler) {
                UrlReplaceHandlerOption handlerOption =
                        ((UrlReplaceHandler) interceptor).getUrlReplaceHandlerOption();
                assertTrue(handlerOption.getReplacementPairs().isEmpty());
                assertFalse(handlerOption.isEnabled());
            }

            assertTrue(
                    interceptor instanceof UrlReplaceHandler
                            || interceptor instanceof RedirectHandler
                            || interceptor instanceof RetryHandler
                            || interceptor instanceof ParametersNameDecodingHandler
                            || interceptor instanceof UserAgentHandler
                            || interceptor instanceof HeadersInspectionHandler
                            || interceptor instanceof ChaosHandler,
                    "Array should contain instances of"
                        + " UrlReplaceHandler,RedirectHandler,RetryHandler,ParametersNameDecodingHandler,UserAgentHandler,"
                        + " HeadersInspectionHandler, and ChaosHandler");
        }
    }

    @Test
    void testCreateWithAuthProviderAndRequestOptions() throws IOException {
        RetryHandlerOption retryHandlerOption =
                new RetryHandlerOption((delay, executionCount, request, response) -> false, 0, 0);
        UrlReplaceHandlerOption urlReplaceHandlerOption =
                new UrlReplaceHandlerOption(new HashMap<>(), false);

        final ArrayList<RequestOption> options = new ArrayList<>();
        options.add(urlReplaceHandlerOption);
        options.add(retryHandlerOption);

        OkHttpClient client =
                KiotaClientFactory.create(
                                new BaseBearerTokenAuthenticationProvider(
                                        mock(AccessTokenProvider.class)),
                                options.toArray(new RequestOption[0]))
                        .build();
        List<Interceptor> clientInterceptors = client.interceptors();
        assertEquals(
                Arrays.asList(
                        UrlReplaceHandler.class,
                        ParametersNameDecodingHandler.class,
                        UserAgentHandler.class,
                        AuthorizationHandler.class,
                        RedirectHandler.class,
                        RetryHandler.class,
                        HeadersInspectionHandler.class),
                clientInterceptors.stream().map(Object::getClass).collect(Collectors.toList()));
        for (Interceptor interceptor : clientInterceptors) {
            if (interceptor instanceof RetryHandler) {
                RetryHandlerOption handlerOption = ((RetryHandler) interceptor).getRetryOptions();
                assertEquals(0, handlerOption.delay());
                assertEquals(0, handlerOption.maxRetries());
            }

            if (interceptor instanceof UrlReplaceHandler) {
                UrlReplaceHandlerOption handlerOption =
                        ((UrlReplaceHandler) interceptor).getUrlReplaceHandlerOption();
                assertTrue(handlerOption.getReplacementPairs().isEmpty());
                assertFalse(handlerOption.isEnabled());
            }
        }
    }

    @Test
    void retriesOnlyTheRedirectedHopAndInspectsTheRetriedRequest() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(
                    new MockResponse().setResponseCode(302).setHeader("Location", "/target"));
            server.enqueue(new MockResponse().setResponseCode(429).setHeader("Retry-After", "0"));
            server.enqueue(new MockResponse().setHeader("X-Result", "final"));
            HeadersInspectionOption inspection = new HeadersInspectionOption(true, true);
            Request request =
                    new Request.Builder()
                            .url(server.url("/initial"))
                            .tag(HeadersInspectionOption.class, inspection)
                            .build();

            try (Response response =
                    KiotaClientFactory.create().build().newCall(request).execute()) {
                assertEquals(200, response.code());
                assertTrue(inspection.getRequestHeaders().get("Retry-Attempt").contains("1"));
                assertTrue(inspection.getResponseHeaders().get("X-Result").contains("final"));
            }
            assertEquals(3, server.getRequestCount());
            assertEquals("/initial", server.takeRequest().getPath());
            assertEquals("/target", server.takeRequest().getPath());
            assertEquals("/target", server.takeRequest().getPath());
        }
    }

    @Test
    void upstreamRedirectHandlingDisabledByDefault() {
        OkHttpClient client = KiotaClientFactory.create().build();
        assertFalse(client.followRedirects());
    }

    private static RetryHandler getDisabledRetryHandler() {
        RetryHandlerOption retryHandlerOption =
                new RetryHandlerOption((delay, executionCount, request, response) -> false, 0, 0);
        RetryHandler retryHandler = new RetryHandler(retryHandlerOption);
        return retryHandler;
    }
}
