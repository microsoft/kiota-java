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
import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KiotaClientFactoryTest {

    @Test
    void testCreatesDefaultInterceptors() throws IOException {
        OkHttpClient client = KiotaClientFactory.create().build();
        assertNotNull(client.interceptors());
        assertEquals(6, client.interceptors().size());
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
        assertNotNull(clientInterceptors);
        // including the Authorization Handler
        assertEquals(7, clientInterceptors.size());
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
                            || interceptor instanceof ChaosHandler
                            || interceptor instanceof AuthorizationHandler,
                    "Array should contain instances of"
                        + " UrlReplaceHandler,RedirectHandler,RetryHandler,ParametersNameDecodingHandler,UserAgentHandler,"
                        + " HeadersInspectionHandler, and ChaosHandler");
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
