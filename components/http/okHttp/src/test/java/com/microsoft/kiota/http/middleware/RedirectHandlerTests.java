package com.microsoft.kiota.http.middleware;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.kiota.http.KiotaClientFactory;
import com.microsoft.kiota.http.middleware.options.RedirectHandlerOption;

import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.Test;

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
    void crossHostRedirectLeaksCookies() throws Exception {
        Request original =
                new Request.Builder()
                        .url("http://trusted.example.com/api")
                        .addHeader("Authorization", "Bearer token")
                        .addHeader("Cookie", "session=SECRET")
                        .addHeader("Proxy-Authorization", "Basic cHJveHk6cGFzcw==")
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
        Request result = new RedirectHandler().getRedirect(original, redirect);
        assertNotNull(result);
        assertEquals("evil.attacker.com", result.url().host());
        assertNull(result.header("Authorization")); // stripped (good)
        assertNull(result.header("Cookie")); // stripped (good)
        assertNull(result.header("Proxy-Authorization")); // stripped (good)
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
}
