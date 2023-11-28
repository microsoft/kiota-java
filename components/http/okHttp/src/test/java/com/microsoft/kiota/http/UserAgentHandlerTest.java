package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import com.microsoft.kiota.http.middleware.UserAgentHandler;
import com.microsoft.kiota.http.middleware.options.UserAgentHandlerOption;

import okhttp3.Headers;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

class UserAgentHandlerTest {
    private final Chain mockChain;
    private final Response mockResponse;

    public UserAgentHandlerTest() throws IOException {
        mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(200);
        when(mockResponse.message()).thenReturn("OK");
        when(mockResponse.body()).thenReturn(mock(ResponseBody.class));
        when(mockResponse.headers()).thenReturn(new Headers.Builder().build());
        mockChain = mock(Chain.class);
        when(mockChain.proceed(any(Request.class)))
                .thenAnswer(
                        new Answer() {
                            public Object answer(InvocationOnMock invocation) {
                                Object[] args = invocation.getArguments();
                                Request request = (Request) args[0];
                                when(mockResponse.request()).thenReturn(request);
                                return mockResponse;
                            }
                        });
    }

    @Test
    void addsTheProduct() throws IOException {
        final UserAgentHandler handler = new UserAgentHandler();
        final Request request = new Request.Builder().url("http://localhost").build();
        when(mockChain.request()).thenReturn(request);
        final Response response = handler.intercept(mockChain);
        final Request result = response.request();
        assertNotNull(response);
        assertNotNull(result);
        assertEquals("kiota-java", result.header("User-Agent").split("/")[0]);
    }

    @Test
    void addsTheProductOnce() throws IOException {
        final UserAgentHandler handler = new UserAgentHandler();
        final Request request = new Request.Builder().url("http://localhost").build();
        when(mockChain.request()).thenReturn(request);
        Response response = handler.intercept(mockChain);
        response = handler.intercept(mockChain);
        final Request result = response.request();
        assertNotNull(response);
        assertNotNull(result);
        assertEquals(1, result.header("User-Agent").split("kiota-java").length - 1);
    }

    @Test
    void doesNotAddTheProductWhenDisabled() throws IOException {
        final UserAgentHandler handler =
                new UserAgentHandler(
                        new UserAgentHandlerOption() {
                            {
                                setEnabled(false);
                            }
                        });
        final Request request = new Request.Builder().url("http://localhost").build();
        when(mockChain.request()).thenReturn(request);
        final Response response = handler.intercept(mockChain);
        final Request result = response.request();
        assertNotNull(response);
        assertNotNull(result);
        assertNull(result.header("User-Agent"));
    }
}
