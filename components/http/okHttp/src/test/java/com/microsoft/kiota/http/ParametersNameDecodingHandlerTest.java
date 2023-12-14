package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.http.middleware.options.ParametersNameDecodingOption;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

class ParametersNameDecodingHandlerTest {

    private static Stream<Arguments> originalAndExpectedUrls() {
        return Stream.of(
                Arguments.of("https://www.google.com/", "https://www.google.com/"),
                Arguments.of("https://www.google.com/?q=1%2B2", "https://www.google.com/?q=1%2B2"),
                Arguments.of("https://www.google.com/?q=M%26A", "https://www.google.com/?q=M%26A"),
                Arguments.of(
                        "https://www.google.com/?q%2D1=M%26A", "https://www.google.com/?q-1=M%26A"),
                Arguments.of(
                        "https://www.google.com/?q%2D1&q=M%26A=M%26A",
                        "https://www.google.com/?q-1&q=M%26A=M%26A"));
    }

    @ParameterizedTest
    @MethodSource("originalAndExpectedUrls")
    void defaultParameterNameDecodingHandlerOnlyDecodesNamesNotValues(
            String original, String expectedResult) throws IOException {
        Interceptor[] interceptors =
                new Interceptor[] {
                    new ParametersNameDecodingHandler(
                            new ParametersNameDecodingOption() {
                                {
                                    parametersToDecode = new char[] {'$', '.', '-', '~', '+', '&'};
                                }
                            })
                };
        final OkHttpClient client = KiotaClientFactory.create(interceptors).build();
        final Request request = new Request.Builder().url(original).build();
        final Response response = client.newCall(request).execute();

        assertNotNull(response);
        assertEquals(expectedResult, response.request().url().toString());
    }
}
