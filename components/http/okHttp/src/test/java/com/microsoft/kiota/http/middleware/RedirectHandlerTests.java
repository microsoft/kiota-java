package com.microsoft.kiota.http.middleware;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.microsoft.kiota.http.KiotaClientFactory;
import com.microsoft.kiota.http.middleware.options.RedirectHandlerOption;

import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.util.Collections;

@SuppressWarnings("resource")
public class RedirectHandlerTests {

    @Test
    void redirectsAreFollowedByDefault() throws Exception {
        var server = new MockWebServer();
        server.enqueue(
                new MockResponse().setResponseCode(301).setHeader("Location", server.url("/bar")));
        server.enqueue(new MockResponse().setResponseCode(201));

        var interceptors = new Interceptor[] {new RedirectHandler()};

        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(server.url("/foo")).build();

        // ACT
        var response = client.newCall(request).execute();

        server.takeRequest(); // discard first request
        var request2 = server.takeRequest();

        assertEquals("/bar", request2.getPath());
        assertEquals(201, response.code());
    }

    @Test
    void redirectsCanBeDisabled() throws Exception {
        var server = new MockWebServer();
        server.enqueue(
                new MockResponse().setResponseCode(301).setHeader("Location", server.url("/bar")));

        var ignoreRedirectsOption = new RedirectHandlerOption(0, response -> false);
        var redirectHandler = new RedirectHandler(ignoreRedirectsOption);
        var interceptors = new Interceptor[] {redirectHandler};

        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(server.url("/foo")).build();

        // ACT
        var response = client.newCall(request).execute();

        assertEquals(301, response.code());
    }

    @Test
    void crossHostRedirectStripsAuthHeaders() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic <secret>")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header("Location", "http://evil.attacker.com/steal")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Mock chain to get client without proxy
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        OkHttpClient client = new OkHttpClient.Builder().build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption();
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);
        assertNotNull(result);
        assertEquals("evil.attacker.com", result.url().host());
        assertNull(result.header("Authorization")); // stripped (good)
        assertNull(result.header("Cookie")); // stripped (good)
        assertNull(result.header("Proxy-Authorization")); // stripped because no proxy (good)
    }

    @Test
    void endToEndProof() throws Exception {
        var evil = new MockWebServer();
        evil.start();
        evil.enqueue(new MockResponse().setResponseCode(200));
        var trusted = new MockWebServer();
        trusted.start();
        trusted.enqueue(
                new MockResponse().setResponseCode(302).setHeader("Location", evil.url("/steal")));
        OkHttpClient client =
                KiotaClientFactory.create(new Interceptor[] {new RedirectHandler()}).build();
        client.newCall(
                        new Request.Builder()
                                .url(trusted.url("/api"))
                                .addHeader("Cookie", "session=SECRET")
                                .build())
                .execute();
        trusted.takeRequest();
        RecordedRequest captured = evil.takeRequest();
        assertNull(captured.getHeader("Cookie"));
        evil.shutdown();
        trusted.shutdown();
    }

    @Test
    void sameHostRedirectKeepsAllHeaders() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic <secret>")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header("Location", "http://trusted.example.com/other")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Mock chain with proxy
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        ProxySelector proxySelector = mock(ProxySelector.class);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
        when(proxySelector.select(any(URI.class))).thenReturn(Collections.singletonList(proxy));

        OkHttpClient client = new OkHttpClient.Builder().proxySelector(proxySelector).build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption();
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);

        assertNotNull(result);
        assertEquals("trusted.example.com", result.url().host());
        assertNotNull(result.header("Authorization")); // kept (same host)
        assertNotNull(result.header("Cookie")); // kept (same host)
        assertNotNull(result.header("Proxy-Authorization")); // kept (proxy is active)
    }

    @Test
    void crossHostRedirectWithProxyKeepsProxyAuth() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic <secret>")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header("Location", "http://other.example.com/api")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Mock chain with active proxy
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        ProxySelector proxySelector = mock(ProxySelector.class);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
        when(proxySelector.select(any(URI.class))).thenReturn(Collections.singletonList(proxy));

        OkHttpClient client = new OkHttpClient.Builder().proxySelector(proxySelector).build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption();
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);

        assertNotNull(result);
        assertEquals("other.example.com", result.url().host());
        assertNull(result.header("Authorization")); // stripped (different host)
        assertNull(result.header("Cookie")); // stripped (different host)
        assertNotNull(
                result.header("Proxy-Authorization")); // KEPT because proxy is still active (good)
    }

    @Test
    void crossHostRedirectWithDirectProxyStripsProxyAuth() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic <secret>")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header("Location", "http://other.example.com/api")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Mock chain with DIRECT proxy (no proxy)
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        ProxySelector proxySelector = mock(ProxySelector.class);
        when(proxySelector.select(any(URI.class)))
                .thenReturn(Collections.singletonList(Proxy.NO_PROXY));

        OkHttpClient client = new OkHttpClient.Builder().proxySelector(proxySelector).build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption();
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);

        assertNotNull(result);
        assertEquals("other.example.com", result.url().host());
        assertNull(result.header("Authorization")); // stripped (different host)
        assertNull(result.header("Cookie")); // stripped (different host)
        assertNull(
                result.header(
                        "Proxy-Authorization")); // stripped because proxy is DIRECT/inactive (good)
    }

    @Test
    void schemeChangeStripsAuthHeaders() throws Exception {
        Request original =
                new Request.Builder()
                        .url("https://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic <secret>")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header(
                                "Location",
                                "http://trusted.example.com/other") // HTTPS -> HTTP (same host)
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Mock chain without proxy
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        OkHttpClient client = new OkHttpClient.Builder().build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption();
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);

        assertNotNull(result);
        assertEquals("trusted.example.com", result.url().host());
        assertEquals("http", result.url().scheme());
        assertNull(result.header("Authorization")); // stripped (scheme changed)
        assertNull(result.header("Cookie")); // stripped (scheme changed)
        assertNull(result.header("Proxy-Authorization")); // stripped (no proxy)
    }

    @Test
    void customScrubberIsUsed() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .build();
        Response redirect =
                new Response.Builder()
                        .request(original)
                        .protocol(Protocol.HTTP_1_1)
                        .code(302)
                        .message("Found")
                        .header("Location", "http://other.example.com/api")
                        .body(ResponseBody.create("", MediaType.parse("text/plain")))
                        .build();

        // Custom scrubber that never removes headers
        RedirectHandlerOption.IScrubSensitiveHeaders customScrubber =
                (requestBuilder, originalUrl, newUrl, proxyResolver) -> {
                    // Don't remove any headers
                };

        // Mock chain
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Call call = mock(Call.class);
        OkHttpClient client = new OkHttpClient.Builder().build();
        when(chain.call()).thenReturn(call);
        when(call.client()).thenReturn(client);

        RedirectHandlerOption option = new RedirectHandlerOption(5, null, customScrubber);
        Request result = new RedirectHandler().getRedirect(original, redirect, option, chain);

        assertNotNull(result);
        assertEquals("other.example.com", result.url().host());
        assertNotNull(result.header("Authorization")); // KEPT by custom scrubber
        assertNotNull(result.header("Cookie")); // KEPT by custom scrubber
    }
}
