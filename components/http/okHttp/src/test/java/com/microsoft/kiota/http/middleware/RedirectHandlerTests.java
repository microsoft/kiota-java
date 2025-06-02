package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.KiotaClientFactory;
import com.microsoft.kiota.http.middleware.options.RedirectHandlerOption;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("resource")
public class RedirectHandlerTests {

    @Test
    void redirectsAreFollowedByDefault() throws Exception {
        var server = new MockWebServer();
        server.enqueue(new MockResponse()
            .setResponseCode(301)
            .setHeader("Location", server.url("/bar"))
        );
        server.enqueue(new MockResponse()
            .setResponseCode(201)
        );

        var interceptors = new Interceptor[] { new RedirectHandler() };

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
        server.enqueue(new MockResponse()
            .setResponseCode(301)
            .setHeader("Location", server.url("/bar"))
        );

        var ignoreRedirectsOption = new RedirectHandlerOption(0, response -> false);
        var redirectHandler = new RedirectHandler(ignoreRedirectsOption);
        var interceptors = new Interceptor[] { redirectHandler };

        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(server.url("/foo")).build();

        // ACT
        var response = client.newCall(request).execute();

        assertEquals(301, response.code());
    }
}
