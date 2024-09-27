package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.mocks.TestEnum;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class RequestInformationTest {
    @Test
    void ThrowsInvalidOperationExceptionWhenBaseUrlNotSet() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "{+baseurl}/users{?%24count}";

        // Assert
        var exception = assertThrows(IllegalStateException.class, () -> requestInfo.getUri());
        assertTrue(exception.getMessage().contains("baseurl"));
    }

    @Test
    void BuildsUrlOnProvidedBaseUrl() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "{+baseurl}/users{?%24count}";

        // Act
        requestInfo.pathParameters.put("baseurl", "http://localhost");

        // Assert
        var result = assertDoesNotThrow(() -> requestInfo.getUri());
        assertEquals("http://localhost/users", result.toString());
    }

    @Test
    void SetsPathParametersOfDateTimeOffsetType() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate =
                "http://localhost/getDirectRoutingCalls(fromDateTime='{fromDateTime}',toDateTime='{toDateTime}')";

        // Act
        final OffsetDateTime fromDateTime =
                OffsetDateTime.of(2022, 8, 1, 0, 0, 0, 0, ZoneOffset.of("+00:00"));
        final OffsetDateTime toDateTime =
                OffsetDateTime.of(2022, 8, 2, 0, 0, 0, 0, ZoneOffset.of("+00:00"));
        requestInfo.pathParameters.put("fromDateTime", fromDateTime);
        requestInfo.pathParameters.put("toDateTime", toDateTime);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("fromDateTime='2022-08-01T00%3A00%3A00Z'"));
        assertTrue(uriResult.toString().contains("toDateTime='2022-08-02T00%3A00%3A00Z'"));
    }

    @Test
    void SetsPathParametersOfLocalDateType() {
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/getCalendarView(startLocalDate='{startLocalDate}',endLocalDate='{endLocalDate}')";

        final LocalDate startLocalDate = LocalDate.of(2022, 8, 1);
        final LocalDate endLocalDate = LocalDate.of(2022, 8, 2);

        requestInfo.pathParameters.put("startLocalDate", startLocalDate);
        requestInfo.pathParameters.put("endLocalDate", endLocalDate);

        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("startLocalDate='2022-08-01'"));
        assertTrue(uriResult.toString().contains("endLocalDate='2022-08-02'"));
    }

    @Test
    void ExpandQueryParametersAfterPathParams() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate =
                "{+baseurl}/users/{id}/list{?async*,page*,size*,orderBy*,search*}";

        // Act
        requestInfo.pathParameters.put("baseurl", "http://localhost:9090");
        requestInfo.pathParameters.put("id", 1);
        requestInfo.addQueryParameter("async", true);
        requestInfo.addQueryParameter("size", 10);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("size=10"));
        assertTrue(uriResult.toString().contains("async=true"));
    }

    @Test
    void SetQueryParametersParametersWhenEmptyString() {

        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users{?%24search}";

        final GetQueryParameters queryParameters = new GetQueryParameters();
        queryParameters.search = "";

        // Act
        requestInfo.addQueryParameters(queryParameters);

        // Assert
        assertEquals("", queryParameters.search);
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("search"));
    }

    @Test
    void DoesNotSetQueryParametersParametersIfEmptyCollection() {

        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users{?%24select}";

        final GetQueryParameters queryParameters = new GetQueryParameters();
        queryParameters.select = new String[] {};
        // Act
        requestInfo.addQueryParameters(queryParameters);

        // Assert
        assertEquals(0, queryParameters.select.length);
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertFalse(uriResult.toString().contains("select"));
    }

    @Test
    void SetsPathParametersOfBooleanType() {

        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users{?%24count}";

        // Act
        var count = true;
        requestInfo.pathParameters.put("%24count", count);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("count=true"));
    }

    @Test
    void SetsPathParametersOfUUIDType() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users/{id}";

        // Act
        var id = java.util.UUID.fromString("f0f351e7-8e5f-4d0e-8f2a-7b5e4b6f4f3e");
        requestInfo.pathParameters.put("id", id);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("users/f0f351e7-8e5f-4d0e-8f2a-7b5e4b6f4f3e"));
    }

    @Test
    void SetsQueryParametersOfUUIDType() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users{?id}";

        // Act
        var id = java.util.UUID.fromString("f0f351e7-8e5f-4d0e-8f2a-7b5e4b6f4f3e");
        requestInfo.addQueryParameter("id", id);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("?id=f0f351e7-8e5f-4d0e-8f2a-7b5e4b6f4f3e"));
    }

    @Test
    void SetsParsableContent() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final RequestAdapter requestAdapterMock = createMockRequestAdapter(writerMock);
        requestInfo.setContentFromParsable(
                requestAdapterMock, "application/json", new TestEntity());

        verify(writerMock, times(1)).writeObjectValue(any(), any(TestEntity.class));
        verify(writerMock, never())
                .writeCollectionOfObjectValues(anyString(), any(ArrayList.class));
    }

    @Test
    void SetsParsableContentCollection() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final RequestAdapter requestAdapterMock = createMockRequestAdapter(writerMock);
        requestInfo.setContentFromParsable(
                requestAdapterMock, "application/json", new TestEntity[] {new TestEntity()});

        verify(writerMock, never()).writeObjectValue(any(), any(TestEntity.class));
        verify(writerMock, times(1)).writeCollectionOfObjectValues(any(), any(Iterable.class));
    }

    @Test
    void SetsScalarContentCollection() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final RequestAdapter requestAdapterMock = createMockRequestAdapter(writerMock);
        requestInfo.setContentFromScalarCollection(
                requestAdapterMock, "application/json", new String[] {"foo"});

        verify(writerMock, never()).writeStringValue(any(), anyString());
        verify(writerMock, times(1)).writeCollectionOfPrimitiveValues(any(), any(Iterable.class));
    }

    @Test
    void SetsScalarContent() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final RequestAdapter requestAdapterMock = createMockRequestAdapter(writerMock);
        requestInfo.setContentFromScalar(requestAdapterMock, "application/json", "foo");

        verify(writerMock, times(1)).writeStringValue(any(), anyString());
        verify(writerMock, never()).writeCollectionOfPrimitiveValues(any(), any(Iterable.class));
    }

    @Test
    void SetsBoundaryOnMultipartBody() {
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.POST;
        requestInfo.urlTemplate =
                "http://localhost/{URITemplate}/ParameterMapping?IsCaseSensitive={IsCaseSensitive}";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final RequestAdapter requestAdapterMock = createMockRequestAdapter(writerMock);

        final MultipartBody multipartBody = new MultipartBody();
        multipartBody.requestAdapter = requestAdapterMock;

        requestInfo.setContentFromParsable(
                requestAdapterMock, "multipart/form-data", multipartBody);
        assertNotNull(multipartBody.getBoundary());
        assertFalse(multipartBody.getBoundary().isEmpty());
        assertEquals(
                "multipart/form-data; boundary=" + multipartBody.getBoundary(),
                requestInfo.headers.get("Content-Type").toArray()[0]);
    }

    @Test
    void ReplacesEnumSingleValueQueryParameters() throws IllegalStateException, URISyntaxException {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/{?dataset}";

        final GetQueryParameters queryParameters = new GetQueryParameters();
        queryParameters.dataset = TestEnum.First;
        // Act
        requestInfo.addQueryParameters(queryParameters);

        // Assert
        final URI uri = requestInfo.getUri();
        assertEquals("http://localhost/?dataset=1", uri.toString());
    }

    @Test
    void ReplacesEnumValuesQueryParameters() throws IllegalStateException, URISyntaxException {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/{?datasets}";

        final GetQueryParameters queryParameters = new GetQueryParameters();
        queryParameters.datasets = new TestEnum[] {TestEnum.First, TestEnum.Second};
        // Act
        requestInfo.addQueryParameters(queryParameters);

        // Assert
        final URI uri = requestInfo.getUri();
        assertEquals("http://localhost/?datasets=1,2", uri.toString());
    }

    @Test
    void ReplacesEnumSingleValuePathParameters() throws IllegalStateException, URISyntaxException {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/{dataset}";

        // Act
        requestInfo.pathParameters.put("dataset", ((Object) (TestEnum.First)));

        // Assert
        final URI uri = requestInfo.getUri();
        assertEquals("http://localhost/1", uri.toString());
    }

    @Test
    void ReplacesEnumValuesPathParameters() throws IllegalStateException, URISyntaxException {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod = HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/{datasets}";

        // Act
        requestInfo.pathParameters.put(
                "datasets", ((Object) (new TestEnum[] {TestEnum.First, TestEnum.Second})));

        // Assert
        final URI uri = requestInfo.getUri();
        assertEquals("http://localhost/1,2", uri.toString());
    }

    public final SerializationWriterFactory createMockSerializationWriterFactory(
            SerializationWriter writer) {
        final SerializationWriterFactory factoryMock = mock(SerializationWriterFactory.class);
        when(factoryMock.getSerializationWriter(anyString())).thenReturn(writer);
        return factoryMock;
    }

    public final RequestAdapter createMockRequestAdapter(SerializationWriterFactory factory) {
        final RequestAdapter requestAdapterMock = mock(RequestAdapter.class);
        when(requestAdapterMock.getSerializationWriterFactory()).thenReturn(factory);
        return requestAdapterMock;
    }

    public final RequestAdapter createMockRequestAdapter(SerializationWriter writer) {
        return createMockRequestAdapter(createMockSerializationWriterFactory(writer));
    }
}

/** The messages in a mailbox or folder. Read-only. Nullable. */
class GetQueryParameters implements QueryParameters {
    /** Select properties to be returned */
    @jakarta.annotation.Nullable public String[] select;

    /** Search items by search phrases */
    @jakarta.annotation.Nullable public String search;

    @jakarta.annotation.Nullable public TestEnum dataset;

    @jakarta.annotation.Nullable public TestEnum[] datasets;

    @jakarta.annotation.Nonnull public Map<String, Object> toQueryParameters() {
        final Map<String, Object> allQueryParams = new HashMap();
        allQueryParams.put("%24select", select);
        allQueryParams.put("%24search", search);
        allQueryParams.put("dataset", dataset);
        allQueryParams.put("datasets", datasets);
        return allQueryParams;
    }
}
