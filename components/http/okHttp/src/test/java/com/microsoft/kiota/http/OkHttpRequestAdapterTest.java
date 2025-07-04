package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.microsoft.kiota.ApiException;
import com.microsoft.kiota.HttpMethod;
import com.microsoft.kiota.NativeResponseHandler;
import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.http.middleware.MockResponseHandler;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.ParseNodeFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import okio.Buffer;
import okio.Okio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class OkHttpRequestAdapterTest {
    @ParameterizedTest
    @EnumSource(
            value = HttpMethod.class,
            names = {"PUT", "POST", "PATCH"})
    void postRequestsShouldHaveEmptyBody(HttpMethod method)
            throws Exception { // Unexpected exception thrown: java.lang.IllegalArgumentException:
        // method POST must have a request body.
        final AuthenticationProvider authenticationProviderMock =
                mock(AuthenticationProvider.class);
        final var adapter =
                new OkHttpRequestAdapter(authenticationProviderMock) {
                    public Request test() throws Exception {
                        RequestInformation ri = new RequestInformation();
                        ri.httpMethod = method;
                        ri.urlTemplate = "http://localhost:1234";
                        Span span1 = GlobalOpenTelemetry.getTracer("").spanBuilder("").startSpan();
                        Span span2 = GlobalOpenTelemetry.getTracer("").spanBuilder("").startSpan();
                        return this.getRequestFromRequestInformation(ri, span1, span2);
                    }
                };

        final var request = assertDoesNotThrow(() -> adapter.test());
        assertNotNull(request.body());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 202, 203, 206})
    void sendStreamReturnsUsableStream(int statusCode) throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var text = "my-demo-text";
        final var bufferedSource =
                Okio.buffer(Okio.source(new ByteArrayInputStream(text.getBytes("UTF-8"))));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(statusCode)
                                .message("OK")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(
                                        ResponseBody.create(
                                                bufferedSource,
                                                MediaType.parse("application/binary"),
                                                text.getBytes("UTF-8").length))
                                .build());
        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, null, null, client);
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        InputStream response = null;
        try {
            response = requestAdapter.sendPrimitive(requestInformation, null, InputStream.class);
            assertNotNull(response);
            assertEquals(text, new String(response.readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 202, 203, 204, 304})
    void sendStreamReturnsNullOnNoContent(int statusCode) throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(statusCode)
                                .message("OK")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(null)
                                .build());
        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, null, null, client);
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var response =
                requestAdapter.sendPrimitive(requestInformation, null, InputStream.class);
        assertNull(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 202, 203, 204, 205, 304})
    void sendReturnsNullOnNoContent(int statusCode) throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(statusCode)
                                .message("OK")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(null)
                                .build());
        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, null, null, client);
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var mockEntity = creatMockEntity();
        final var response = requestAdapter.send(requestInformation, null, (node) -> mockEntity);
        assertNull(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 202, 203})
    void sendReturnsObjectOnContent(int statusCode) throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(statusCode)
                                .message("OK")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(
                                        ResponseBody.create(
                                                "test".getBytes("UTF-8"),
                                                MediaType.parse("application/json")))
                                .build());
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var mockEntity = creatMockEntity();
        final var mockParseNode = creatMockParseNode(mockEntity);
        final var mockFactory = creatMockParseNodeFactory(mockParseNode, "application/json");
        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, mockFactory, null, client);
        final var response = requestAdapter.send(requestInformation, null, (node) -> mockEntity);
        assertNotNull(response);
    }

    private static Stream<Arguments> providesErrorMappings() {
        return Stream.of(
                // unexpected error code exception
                Arguments.of(404, null, false),
                Arguments.of(400, Arrays.asList("5XX"), false),
                Arguments.of(503, null, false),
                Arguments.of(502, Arrays.asList("4XX"), false),
                Arguments.of(502, Arrays.asList(""), false),
                // expect deserialized exception
                Arguments.of(404, Arrays.asList("404"), true),
                Arguments.of(500, Arrays.asList("500"), true),
                Arguments.of(404, Arrays.asList("XXX"), true),
                Arguments.of(500, Arrays.asList("XXX"), true),
                Arguments.of(404, Arrays.asList("5XX", "XXX"), true),
                Arguments.of(500, Arrays.asList("4XX", "XXX"), true));
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("providesErrorMappings")
    void throwsAPIException(
            int responseStatusCode,
            List<String> errorMappingCodes,
            boolean expectDeserializedException)
            throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(responseStatusCode)
                                .message("Not Found")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(
                                        ResponseBody.create(
                                                "test".getBytes("UTF-8"),
                                                MediaType.parse("application/json")))
                                .header("request-id", "request-id-value")
                                .build());
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var mockEntity = creatMockEntity();
        final var mockParsableFactory = mock(ParsableFactory.class);
        when(mockParsableFactory.create(any(ParseNode.class))).thenReturn(mockEntity);
        final var mockParseNode = creatMockParseNode(mockEntity);
        final var mockFactory = creatMockParseNodeFactory(mockParseNode, "application/json");

        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, mockFactory, null, client);
        final var errorMappings =
                errorMappingCodes == null
                        ? null
                        : new HashMap<String, ParsableFactory<? extends Parsable>>();
        if (errorMappings != null)
            errorMappingCodes.forEach((mapping) -> errorMappings.put(mapping, mockParsableFactory));
        final var exception =
                assertThrows(
                        ApiException.class,
                        () ->
                                requestAdapter.send(
                                        requestInformation, errorMappings, (node) -> mockEntity));
        assertNotNull(exception);
        if (expectDeserializedException)
            verify(mockParseNode, times(1)).getObjectValue(mockParsableFactory);
        assertEquals(responseStatusCode, exception.getResponseStatusCode());
        assertTrue(exception.getResponseHeaders().containsKey("request-id"));
    }

    @Test
    void getRequestFromRequestInformationHasCorrectContentLength_JsonPayload() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        ByteArrayInputStream content =
                new ByteArrayInputStream(
                        "{\"name\":\"value\",\"array\":[\"1\",\"2\",\"3\"]}"
                                .getBytes(StandardCharsets.UTF_8));
        requestInformation.setStreamContent(content, "application/json");
        requestInformation.httpMethod = HttpMethod.PUT;
        final var contentLength = content.available();
        requestInformation.headers.tryAdd("Content-Length", String.valueOf(contentLength));

        final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
        final var request =
                adapter.getRequestFromRequestInformation(
                        requestInformation, mock(Span.class), mock(Span.class));

        assertEquals(String.valueOf(contentLength), request.headers().get("Content-Length"));
        assertEquals("application/json", request.headers().get("Content-Type"));
        assertNotNull(request.body());
        assertEquals(request.body().contentLength(), contentLength);
        assertEquals(request.body().contentType(), MediaType.parse("application/json"));
    }

    @Test
    void getRequestFromRequestInformationIncludesContentLength_FilePayload() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var testFile = new File("./src/test/resources/helloWorld.txt");
        final var requestInformation = new RequestInformation();

        requestInformation.setUri(new URI("https://localhost"));
        requestInformation.httpMethod = HttpMethod.PUT;
        final var contentLength = testFile.length();
        requestInformation.headers.add("Content-Length", String.valueOf(contentLength));
        try (FileInputStream content = new FileInputStream(testFile)) {
            requestInformation.setStreamContent(content, "application/octet-stream");

            final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
            final var request =
                    adapter.getRequestFromRequestInformation(
                            requestInformation, mock(Span.class), mock(Span.class));

            assertEquals(String.valueOf(contentLength), request.headers().get("Content-Length"));
            assertEquals("application/octet-stream", request.headers().get("Content-Type"));
            assertNotNull(request.body());
            assertEquals(request.body().contentLength(), contentLength);
            assertEquals(request.body().contentType(), MediaType.parse("application/octet-stream"));
        }
    }

    @Test
    void getRequestFromRequestInformationWithoutContentLengthOverrideForStreamBody()
            throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var testFile = new File("./src/test/resources/helloWorld.txt");
        final var requestInformation = new RequestInformation();

        requestInformation.setUri(new URI("https://localhost"));
        requestInformation.httpMethod = HttpMethod.PUT;
        try (FileInputStream content = new FileInputStream(testFile)) {
            requestInformation.setStreamContent(content, "application/octet-stream");

            final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
            final var request =
                    adapter.getRequestFromRequestInformation(
                            requestInformation, mock(Span.class), mock(Span.class));

            assertEquals("application/octet-stream", request.headers().get("Content-Type"));
            assertNotNull(request.body());
            assertEquals(-1L, request.body().contentLength());
            assertEquals(request.body().contentType(), MediaType.parse("application/octet-stream"));
        }
    }

    @Test
    void getRequestFromRequestInformationWithoutContentLengthOverrideForJsonPayload()
            throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        ByteArrayInputStream content =
                new ByteArrayInputStream(
                        "{\"name\":\"value\",\"array\":[\"1\",\"2\",\"3\"]}"
                                .getBytes(StandardCharsets.UTF_8));
        requestInformation.setStreamContent(content, "application/json");
        requestInformation.httpMethod = HttpMethod.PUT;
        final var contentLength = content.available();

        final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
        final var request =
                adapter.getRequestFromRequestInformation(
                        requestInformation, mock(Span.class), mock(Span.class));

        assertEquals("application/json", request.headers().get("Content-Type"));
        assertNotNull(request.body());
        assertEquals(contentLength, request.body().contentLength());
        assertEquals(MediaType.parse("application/json"), request.body().contentType());
    }

    @Test
    void getRequestFromRequestInformationWithoutContentLengthOverrideWithEmptyPayload()
            throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);
        requestInformation.httpMethod = HttpMethod.PUT;
        requestInformation.content = content;

        final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
        final var request =
                adapter.getRequestFromRequestInformation(
                        requestInformation, mock(Span.class), mock(Span.class));

        assertNull(request.headers().get("Content-Type"));
        assertEquals(0, request.body().contentLength());
    }

    @Test
    void buildsNativeRequestSupportingMultipleWrites() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        var requestBodyJson = "{\"name\":\"value\",\"array\":[\"1\",\"2\",\"3\"]}";
        ByteArrayInputStream content =
                new ByteArrayInputStream(requestBodyJson.getBytes(StandardCharsets.UTF_8));
        requestInformation.setStreamContent(content, "application/json");
        requestInformation.httpMethod = HttpMethod.PUT;

        final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
        final var request =
                adapter.getRequestFromRequestInformation(
                        requestInformation, mock(Span.class), mock(Span.class));

        final var requestBody = request.body();
        assertNotNull(requestBody);
        var buffer = new Buffer();
        requestBody.writeTo(buffer);
        assertEquals(requestBodyJson, buffer.readUtf8());

        // Second write to the buffer to ensure the body is not consumed
        buffer = new Buffer();
        requestBody.writeTo(buffer);
        assertEquals(requestBodyJson, buffer.readUtf8());
    }

    @Test
    void buildsNativeRequestSupportingOneShotWrite() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        final var testFile = new File("./src/test/resources/helloWorld.txt");
        final var requestInformation = new RequestInformation();

        requestInformation.setUri(new URI("https://localhost"));
        requestInformation.httpMethod = HttpMethod.PUT;
        final var contentLength = testFile.length();
        requestInformation.headers.add("Content-Length", String.valueOf(contentLength));
        try (FileInputStream content = new FileInputStream(testFile)) {
            requestInformation.setStreamContent(content, "application/octet-stream");

            final var adapter = new OkHttpRequestAdapter(authenticationProviderMock);
            final var request =
                    adapter.getRequestFromRequestInformation(
                            requestInformation, mock(Span.class), mock(Span.class));

            final var requestBody = request.body();
            assertNotNull(requestBody);
            var buffer = new Buffer();
            requestBody.writeTo(buffer);
            assertEquals(contentLength, buffer.size());

            // Second write to the buffer to ensure the body is not consumed
            buffer = new Buffer();
            requestBody.writeTo(buffer);
            assertEquals(0, buffer.size());
        }
    }

    @Test
    void loggingInterceptorDoesNotDrainRequestBodyForMarkableStreams() throws Exception {
        var loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.BODY);

        var okHttpClient =
                KiotaClientFactory.create()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor(new MockResponseHandler())
                        .build();

        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, null, null, okHttpClient);

        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        var requestBodyJson = "{\"name\":\"value\",\"array\":[\"1\",\"2\",\"3\"]}";
        ByteArrayInputStream content =
                new ByteArrayInputStream(requestBodyJson.getBytes(StandardCharsets.UTF_8));
        requestInformation.setStreamContent(content, "application/json");
        requestInformation.httpMethod = HttpMethod.PUT;
        var nativeResponseHandler = new NativeResponseHandler();
        requestInformation.setResponseHandler(nativeResponseHandler);

        var mockEntity = creatMockEntity();
        requestAdapter.send(requestInformation, null, node -> mockEntity);
        var nativeResponse = (Response) nativeResponseHandler.getValue();
        assertNotNull(nativeResponse);
        assertEquals(requestBodyJson, nativeResponse.body().source().readUtf8());
    }

    @Test
    void loggingInterceptorDoesNotDrainRequestBodyForNonMarkableStreams() throws Exception {
        var loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.BODY);

        var okHttpClient =
                KiotaClientFactory.create()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor(new MockResponseHandler())
                        .build();

        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, null, null, okHttpClient);

        final var requestInformation = new RequestInformation();
        requestInformation.setUri(new URI("https://localhost"));
        requestInformation.httpMethod = HttpMethod.PUT;
        var nativeResponseHandler = new NativeResponseHandler();
        requestInformation.setResponseHandler(nativeResponseHandler);

        final var testFile = new File("./src/test/resources/helloWorld.txt");
        final var contentLength = testFile.length();

        try (FileInputStream content = new FileInputStream(testFile)) {
            requestInformation.setStreamContent(content, "application/octet-stream");
            var mockEntity = creatMockEntity();
            requestAdapter.send(requestInformation, null, node -> mockEntity);
            var nativeResponse = (Response) nativeResponseHandler.getValue();
            assertNotNull(nativeResponse);
            assertEquals(contentLength, nativeResponse.body().source().readByteArray().length);
        }
    }

    @Test
    void testHandle3XXResponseWithoutLocationHeader() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(301)
                                .message("Moved Permanently")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .body(
                                        ResponseBody.create(
                                                "test".getBytes("UTF-8"),
                                                MediaType.parse("application/json")))
                                .build());
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var mockEntity = creatMockEntity();
        final var mockParseNode = creatMockParseNode(mockEntity);
        final var mockFactory = creatMockParseNodeFactory(mockParseNode, "application/json");

        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, mockFactory, null, client);
        var nativeResponseHandler = new NativeResponseHandler();
        requestAdapter.send(requestInformation, null, node -> mockEntity);
        var nativeResponse = (Response) nativeResponseHandler.getValue();
        assertNull(nativeResponse);
    }

    @Test
    void handle3XXResponseWithLocationHeader() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        authenticationProviderMock.authenticateRequest(
                any(RequestInformation.class), any(Map.class));
        final var client =
                getMockClient(
                        new Response.Builder()
                                .code(301)
                                .message("Moved Permanently")
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost").build())
                                .header("Location", "https://newlocation")
                                .body(
                                        ResponseBody.create(
                                                "test".getBytes("UTF-8"),
                                                MediaType.parse("application/json")))
                                .build());
        final var requestInformation =
                new RequestInformation() {
                    {
                        setUri(new URI("https://localhost"));
                        httpMethod = HttpMethod.GET;
                    }
                };
        final var mockEntity = creatMockEntity();
        final var mockParseNode = creatMockParseNode(mockEntity);
        final var mockFactory = creatMockParseNodeFactory(mockParseNode, "application/json");

        final var requestAdapter =
                new OkHttpRequestAdapter(authenticationProviderMock, mockFactory, null, client);
        var nativeResponseHandler = new NativeResponseHandler();
        requestAdapter.send(requestInformation, null, node -> mockEntity);
        // Should throw an exception
        var nativeResponse = (Response) nativeResponseHandler.getValue();
        assertNull(nativeResponse);
    }

    public static OkHttpClient getMockClient(final Response response) throws IOException {
        final OkHttpClient mockClient = mock(OkHttpClient.class);
        final Call remoteCall = mock(Call.class);
        final Dispatcher dispatcher = new Dispatcher();
        when(remoteCall.execute()).thenReturn(response);
        doAnswer(
                        (Answer<Void>)
                                invocation -> {
                                    Callback callback = invocation.getArgument(0);
                                    callback.onResponse(null, response);
                                    return null;
                                })
                .when(remoteCall)
                .enqueue(any(Callback.class));
        when(mockClient.dispatcher()).thenReturn(dispatcher);
        when(mockClient.newCall(any())).thenReturn(remoteCall);
        return mockClient;
    }

    public Parsable creatMockEntity() {
        final var mockEntity = mock(Parsable.class);
        when(mockEntity.getFieldDeserializers()).thenReturn(new HashMap<>());
        return mockEntity;
    }

    public ParseNode creatMockParseNode(Parsable entity) {
        final var mockParseNode = mock(ParseNode.class);
        when(mockParseNode.getObjectValue(any(ParsableFactory.class))).thenReturn(entity);
        return mockParseNode;
    }

    public ParseNodeFactory creatMockParseNodeFactory(
            ParseNode mockParseNode, String validContentType) {
        final var mockFactory = mock(ParseNodeFactory.class);
        when(mockFactory.getParseNode(any(String.class), any(InputStream.class)))
                .thenReturn(mockParseNode);
        when(mockFactory.getValidContentType()).thenReturn(validContentType);
        return mockFactory;
    }
}
