package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.microsoft.kiota.http.middleware.options.UserAgentHandlerOption;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

import okhttp3.Headers;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
        handler.intercept(mockChain);
        Response response = handler.intercept(mockChain);
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

    @Test
    void enclosesProceedAndPropagatesSpan() throws IOException {
        try (MockedStatic<GlobalOpenTelemetry> otelMock = mockStatic(GlobalOpenTelemetry.class)) {
            final Tracer mockTracer = mock(Tracer.class);
            final SpanBuilder mockSpanBuilder = mock(SpanBuilder.class);
            final Span mockSpan = mock(Span.class);
            final io.opentelemetry.context.Scope mockScope =
                    mock(io.opentelemetry.context.Scope.class);

            otelMock.when(() -> GlobalOpenTelemetry.getTracer(anyString())).thenReturn(mockTracer);
            when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
            when(mockSpanBuilder.setParent(any())).thenReturn(mockSpanBuilder);
            when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
            when(mockSpan.makeCurrent()).thenReturn(mockScope);

            final com.microsoft.kiota.http.ObservabilityOptions obsOptions =
                    new com.microsoft.kiota.http.ObservabilityOptions();
            final UserAgentHandler handler = new UserAgentHandler();
            final Request request =
                    new Request.Builder()
                            .url("http://localhost")
                            .tag(com.microsoft.kiota.http.ObservabilityOptions.class, obsOptions)
                            .build();
            when(mockChain.request()).thenReturn(request);
            when(mockChain.proceed(any(Request.class)))
                    .thenAnswer(
                            invocation -> {
                                final Request req = invocation.getArgument(0);
                                assertSame(mockSpan, req.tag(Span.class));
                                verify(mockSpan, never()).end();
                                verify(mockScope, never()).close();
                                when(mockResponse.request()).thenReturn(req);
                                return mockResponse;
                            });

            final Response response = handler.intercept(mockChain);
            assertNotNull(response);
            verify(mockScope).close();
            verify(mockSpan).end();
        }
    }

    @Test
    void enclosesProceedAndCleansUpOnException() throws IOException {
        try (MockedStatic<GlobalOpenTelemetry> otelMock = mockStatic(GlobalOpenTelemetry.class)) {
            final Tracer mockTracer = mock(Tracer.class);
            final SpanBuilder mockSpanBuilder = mock(SpanBuilder.class);
            final Span mockSpan = mock(Span.class);
            final io.opentelemetry.context.Scope mockScope =
                    mock(io.opentelemetry.context.Scope.class);

            otelMock.when(() -> GlobalOpenTelemetry.getTracer(anyString())).thenReturn(mockTracer);
            when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
            when(mockSpanBuilder.setParent(any())).thenReturn(mockSpanBuilder);
            when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
            when(mockSpan.makeCurrent()).thenReturn(mockScope);

            final com.microsoft.kiota.http.ObservabilityOptions obsOptions =
                    new com.microsoft.kiota.http.ObservabilityOptions();
            final UserAgentHandler handler = new UserAgentHandler();
            final Request request =
                    new Request.Builder()
                            .url("http://localhost")
                            .tag(com.microsoft.kiota.http.ObservabilityOptions.class, obsOptions)
                            .build();
            when(mockChain.request()).thenReturn(request);
            when(mockChain.proceed(any(Request.class)))
                    .thenAnswer(
                            invocation -> {
                                final Request req = invocation.getArgument(0);
                                assertSame(mockSpan, req.tag(Span.class));
                                verify(mockSpan, never()).end();
                                verify(mockScope, never()).close();
                                throw new IOException("network failure");
                            });

            assertThrows(IOException.class, () -> handler.intercept(mockChain));
            verify(mockScope).close();
            verify(mockSpan).end();
        }
    }
}
