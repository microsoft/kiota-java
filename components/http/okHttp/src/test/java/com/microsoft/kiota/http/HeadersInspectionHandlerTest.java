package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.http.middleware.HeadersInspectionHandler;
import com.microsoft.kiota.http.middleware.options.HeadersInspectionOption;

import okhttp3.Headers;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

class HeadersInspectionHandlerTest {
    private final Chain mockChain;
    private final Response mockResponse;

    public HeadersInspectionHandlerTest() throws IOException {
        mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(200);
        when(mockResponse.message()).thenReturn("OK");
        when(mockResponse.body()).thenReturn(mock(ResponseBody.class));
        when(mockResponse.headers()).thenReturn(new Headers.Builder().add("test", "test").build());
        mockChain = mock(Chain.class);
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
    }

    @Test
    void instantiatesWithDefaults() {
        final HeadersInspectionHandler handler = new HeadersInspectionHandler();
        assertNotNull(handler);
    }

    @Test
    void getsRequestHeaders() throws IOException {
        final HeadersInspectionOption option = new HeadersInspectionOption(true, false);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);
        final Request request =
                new Request.Builder().url("http://localhost").addHeader("test", "test").build();
        when(mockChain.request()).thenReturn(request);
        handler.intercept(mockChain);
        assertNotNull(option.getRequestHeaders());
        assertNotNull(option.getResponseHeaders());
        assertEquals(1, option.getRequestHeaders().size());
        assertEquals(0, option.getResponseHeaders().size());
        assertEquals("test", option.getRequestHeaders().get("test").toArray()[0]);
    }

    @Test
    void getsResponseHeaders() throws IOException {
        final HeadersInspectionOption option = new HeadersInspectionOption(false, true);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);
        final Request request =
                new Request.Builder().url("http://localhost").addHeader("test", "test").build();
        when(mockChain.request()).thenReturn(request);
        handler.intercept(mockChain);
        assertNotNull(option.getRequestHeaders());
        assertNotNull(option.getResponseHeaders());
        assertEquals(0, option.getRequestHeaders().size());
        assertEquals(1, option.getResponseHeaders().size());
        assertEquals("test", option.getResponseHeaders().get("test").toArray()[0]);
    }
}
