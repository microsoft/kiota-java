package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.microsoft.kiota.http.KiotaClientFactory;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.HashMap;

class UrlReplaceHandlerTest {

    private static final String DEFAULT_URL_WITH_TOKEN =
            "https://graph.microsoft.com/v1.0/users/TokenToReplace";
    private static final HashMap<String, String> defaultReplacementPairs = new HashMap<>();

    @Test
    void testUrlReplaceHandler_no_replacementPairs() throws IOException {
        Interceptor[] interceptors =
                new Interceptor[] {new UrlReplaceHandler(new UrlReplaceHandlerOption())};
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(DEFAULT_URL_WITH_TOKEN).build();
        final Response response = client.newCall(request).execute();

        assertNotNull(response);
        assertEquals(
                DEFAULT_URL_WITH_TOKEN,
                response.request()
                        .url()
                        .toString()); // url should remain the same without replacement pairs
    }

    @Test
    void testUrlReplaceHandler_default_url() throws IOException {
        defaultReplacementPairs.put("/users/TokenToReplace", "/me");
        Interceptor[] interceptors =
                new Interceptor[] {
                    new UrlReplaceHandler(new UrlReplaceHandlerOption(defaultReplacementPairs))
                };
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(DEFAULT_URL_WITH_TOKEN).build();
        final Response response = client.newCall(request).execute();
        final String expectedNewUrl = "https://graph.microsoft.com/v1.0/me";

        assertNotNull(response);
        assertEquals(expectedNewUrl, response.request().url().toString());
    }

    @Test
    void testUrlReplaceHandler_multiple_pairs() throws IOException {
        defaultReplacementPairs.put("/users/TokenToReplace", "/me");
        defaultReplacementPairs.put("%7BsecondToken%7D", "expectedValue");
        // we're not decoding the URL to avoid mangling base64 encoded ids like
        // events/messages/etc...so the source has to be encoded
        String customUrl =
                "https://graph.microsoft.com/beta/users/TokenToReplace/{secondToken}"; // using
        // special
        // characters
        // to test
        // decoding.
        Interceptor[] interceptors =
                new Interceptor[] {
                    new UrlReplaceHandler(new UrlReplaceHandlerOption(defaultReplacementPairs))
                };
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(customUrl).build();
        final Response response = client.newCall(request).execute();
        final String expectedNewUrl = "https://graph.microsoft.com/beta/me/expectedValue";

        assertNotNull(response);
        assertEquals(expectedNewUrl, response.request().url().toString());
    }

    @Test
    void testItDoesNotReplaceTokensInThePath() throws IOException {
        defaultReplacementPairs.put("/users/TokenToReplace", "/me");
        String customUrl =
                "https://graph.microsoft.com/v1.0/users/TokenToReplace/calendar/events/AAMkADM4Njk4YzMzLTc3YTktNGQwZi05ZjRjLTJlNTUzYWMxZjAxNABGAAAAAABzQLabgoJzT6ewNe8DeydoBwBoFwNVrXtLQ5PleemFoZ87AAAAAAENAABoFwNVrXtLQ5PleemFoZ87AAA33T-eAAA%3D";
        Interceptor[] interceptors =
                new Interceptor[] {
                    new UrlReplaceHandler(new UrlReplaceHandlerOption(defaultReplacementPairs))
                };
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(customUrl).build();
        final Response response = client.newCall(request).execute();
        assertNotNull(response);
        assertEquals(
                "https://graph.microsoft.com/v1.0/me/calendar/events/AAMkADM4Njk4YzMzLTc3YTktNGQwZi05ZjRjLTJlNTUzYWMxZjAxNABGAAAAAABzQLabgoJzT6ewNe8DeydoBwBoFwNVrXtLQ5PleemFoZ87AAAAAAENAABoFwNVrXtLQ5PleemFoZ87AAA33T-eAAA%3D",
                response.request().url().toString());
    }

    @Test
    void testUrlReplaceHandler_propagatesSpan() throws IOException {
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
            final Interceptor.Chain mockChain = mock(Interceptor.Chain.class);
            final Response mockResponse = mock(Response.class);
            defaultReplacementPairs.put("/users/TokenToReplace", "/me");
            final UrlReplaceHandler handler =
                    new UrlReplaceHandler(new UrlReplaceHandlerOption(defaultReplacementPairs));
            final Request request =
                    new Request.Builder()
                            .url(DEFAULT_URL_WITH_TOKEN)
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
                                return mockResponse;
                            });

            final Response response = handler.intercept(mockChain);
            assertNotNull(response);
            verify(mockScope).close();
            verify(mockSpan).end();
        }
    }

    @Test
    void testUrlReplaceHandler_cleansUpSpanOnException() throws IOException {
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
            final Interceptor.Chain mockChain = mock(Interceptor.Chain.class);
            defaultReplacementPairs.put("/users/TokenToReplace", "/me");
            final UrlReplaceHandler handler =
                    new UrlReplaceHandler(new UrlReplaceHandlerOption(defaultReplacementPairs));
            final Request request =
                    new Request.Builder()
                            .url(DEFAULT_URL_WITH_TOKEN)
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
