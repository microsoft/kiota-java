package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.http.middleware.ChaosHandler;
import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.RedirectHandler;
import com.microsoft.kiota.http.middleware.RetryHandler;
import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.UserAgentHandler;
import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;
import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

public class KiotaClientFactoryTest {

    @Test
    void testCreatesDefaultInterceptors() throws IOException {
        OkHttpClient client = KiotaClientFactory.create().build();
        assertNotNull(client.interceptors());
        assertEquals(6, client.interceptors().size());
    }

    @Test
    void testOverideDefaultInterceptors() throws IOException {
        OkHttpClient client = KiotaClientFactory.create(
            new Interceptor[]{getDisabledRetryHandler(), new ChaosHandler()}).build();
        List<Interceptor> interceptors = client.interceptors();
        assertNotNull(interceptors);
        assertEquals(7, interceptors.size());
        for (Interceptor interceptor : interceptors) {
            if (interceptor instanceof RetryHandler) {
                RetryHandlerOption handlerOption = ((RetryHandler) interceptor).getRetryOptions();
                assertEquals(0, handlerOption.delay());
                assertEquals(0, handlerOption.maxRetries());
            }

            assertTrue(interceptor instanceof UrlReplaceHandler || interceptor instanceof RedirectHandler ||
                    interceptor instanceof RetryHandler || interceptor instanceof ParametersNameDecodingHandler
                    || interceptor instanceof UserAgentHandler || interceptor instanceof HeadersInspectionHandler
                    || interceptor instanceof ChaosHandler,
                "Array should contain instances of UrlReplaceHandler,RedirectHandler,RetryHandler,ParametersNameDecodingHandler,UserAgentHandler, HeadersInspectionHandler, and ChaosHandler");


        }


    }

    private static RetryHandler getDisabledRetryHandler() {
        RetryHandlerOption retryHandlerOption = new RetryHandlerOption(
            (delay, executionCount, request, response) -> false, 0, 0);
        RetryHandler retryHandler = new RetryHandler(retryHandlerOption);
        return retryHandler;
    }

}