package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

class UrlReplaceHandlerTest {

    private static final String defaultUsersWithTokenUrl =
            "https://graph.microsoft.com/v1.0/users/TokenToReplace";
    private static final HashMap<String, String> defaultReplacementPairs = new HashMap<>();

    @Test
    void testUrlReplaceHandler_no_replacementPairs() throws IOException {
        Interceptor[] interceptors =
                new Interceptor[] {new UrlReplaceHandler(new UrlReplaceHandlerOption())};
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(defaultUsersWithTokenUrl).build();
        final Response response = client.newCall(request).execute();

        assertNotNull(response);
        assertEquals(
                defaultUsersWithTokenUrl,
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
        final Request request = new Request.Builder().url(defaultUsersWithTokenUrl).build();
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
}
