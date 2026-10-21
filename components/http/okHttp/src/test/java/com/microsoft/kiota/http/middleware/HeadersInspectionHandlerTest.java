package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Test
    void preservesMultiValuedHeaders() throws IOException {
        final HeadersInspectionOption option = new HeadersInspectionOption(true, true);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);
        final Request request =
                new Request.Builder()
                        .url("http://localhost")
                        .addHeader("multi", "val1")
                        .addHeader("multi", "val2")
                        .build();
        when(mockChain.request()).thenReturn(request);
        when(mockResponse.headers())
                .thenReturn(
                        new Headers.Builder()
                                .add("resp-multi", "resp1")
                                .add("resp-multi", "resp2")
                                .build());

        handler.intercept(mockChain);

        assertEquals(1, option.getRequestHeaders().size());
        assertEquals(2, option.getRequestHeaders().get("multi").size());
        assertTrue(option.getRequestHeaders().get("multi").contains("val1"));
        assertTrue(option.getRequestHeaders().get("multi").contains("val2"));

        assertEquals(1, option.getResponseHeaders().size());
        assertEquals(2, option.getResponseHeaders().get("resp-multi").size());
        assertTrue(option.getResponseHeaders().get("resp-multi").contains("resp1"));
        assertTrue(option.getResponseHeaders().get("resp-multi").contains("resp2"));
    }

    @Test
    void clearsHeadersBetweenConsecutiveRequests() throws IOException {
        final HeadersInspectionOption option = new HeadersInspectionOption(true, true);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);

        final Request request1 =
                new Request.Builder().url("http://localhost").addHeader("first", "val1").build();
        when(mockChain.request()).thenReturn(request1);
        when(mockResponse.headers())
                .thenReturn(new Headers.Builder().add("resp-first", "r1").build());
        handler.intercept(mockChain);

        assertTrue(option.getRequestHeaders().containsKey("first"));
        assertTrue(option.getResponseHeaders().containsKey("resp-first"));

        final Request request2 =
                new Request.Builder().url("http://localhost").addHeader("second", "val2").build();
        when(mockChain.request()).thenReturn(request2);
        when(mockResponse.headers())
                .thenReturn(new Headers.Builder().add("resp-second", "r2").build());
        handler.intercept(mockChain);

        assertFalse(option.getRequestHeaders().containsKey("first"));
        assertTrue(option.getRequestHeaders().containsKey("second"));
        assertFalse(option.getResponseHeaders().containsKey("resp-first"));
        assertTrue(option.getResponseHeaders().containsKey("resp-second"));
    }

    @Test
    void clearsResponseHeadersWhenRequestFails() throws IOException {
        final HeadersInspectionOption option = new HeadersInspectionOption(true, true);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);

        final Request successRequest =
                new Request.Builder().url("http://localhost").addHeader("first", "val1").build();
        when(mockChain.request()).thenReturn(successRequest);
        when(mockResponse.headers())
                .thenReturn(new Headers.Builder().add("resp-header", "resp-val").build());
        handler.intercept(mockChain);

        assertEquals(1, option.getResponseHeaders().size());
        assertTrue(option.getResponseHeaders().containsKey("resp-header"));

        final Chain failingChain = mock(Chain.class);
        final Request failingRequest =
                new Request.Builder().url("http://localhost").addHeader("second", "val2").build();
        when(failingChain.request()).thenReturn(failingRequest);
        when(failingChain.proceed(any(Request.class))).thenThrow(new IOException("Network error"));

        assertThrows(IOException.class, () -> handler.intercept(failingChain));

        assertEquals(0, option.getResponseHeaders().size());
        assertEquals(1, option.getRequestHeaders().size());
        assertTrue(option.getRequestHeaders().containsKey("second"));
    }

    @Test
    void handlesConcurrentCallsWithoutRaces() throws InterruptedException, ExecutionException {
        final HeadersInspectionOption option = new HeadersInspectionOption(true, true);
        final HeadersInspectionHandler handler = new HeadersInspectionHandler(option);
        final int threadCount = 8;
        final int iterationsPerThread = 20;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        boolean terminated = false;
        try {
            final List<Callable<Void>> tasks = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                tasks.add(
                        () -> {
                            for (int j = 0; j < iterationsPerThread; j++) {
                                final Request req =
                                        new Request.Builder()
                                                .url("http://localhost")
                                                .addHeader("header-" + threadId, "val-" + j)
                                                .addHeader("shared", "s-" + threadId)
                                                .build();
                                final Chain chain = mock(Chain.class);
                                final Response resp = mock(Response.class);
                                when(resp.headers())
                                        .thenReturn(
                                                new Headers.Builder()
                                                        .add("resp-" + threadId, "rval-" + j)
                                                        .add("resp-shared", "rs-" + threadId)
                                                        .build());
                                when(chain.request()).thenReturn(req);
                                when(chain.proceed(any(Request.class))).thenReturn(resp);

                                handler.intercept(chain);
                            }
                            return null;
                        });
            }

            final List<Future<Void>> futures = executor.invokeAll(tasks);
            for (final Future<Void> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
            terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) {
                executor.shutdownNow();
            }
        }
        assertTrue(terminated);

        assertNotNull(option.getRequestHeaders());
        assertNotNull(option.getResponseHeaders());
    }
}
