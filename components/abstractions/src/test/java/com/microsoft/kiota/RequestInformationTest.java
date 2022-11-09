package com.microsoft.kiota;

import org.junit.jupiter.api.Test;

import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.serialization.SerializationWriterFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestInformationTest {
    @Test
    public void ThrowsInvalidOperationExceptionWhenBaseUrlNotSet()
    {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.GET;
        requestInfo.urlTemplate = "{+baseurl}/users{?%24count}";

        // Assert
        var exception = assertThrows(IllegalStateException.class, () -> requestInfo.getUri());
        assertTrue(exception.getMessage().contains("baseurl"));
    }

    @Test
    public void BuildsUrlOnProvidedBaseUrl()
    {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.GET;
        requestInfo.urlTemplate = "{+baseurl}/users{?%24count}";

        // Act
        requestInfo.pathParameters.put("baseurl","http://localhost");

        // Assert
        var result = assertDoesNotThrow(() -> requestInfo.getUri());
        assertEquals("http://localhost/users", result.toString());
    }

    @Test
    public void SetsPathParametersOfDateTimeOffsetType()
    {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/getDirectRoutingCalls(fromDateTime='{fromDateTime}',toDateTime='{toDateTime}')";

        // Act
        final OffsetDateTime fromDateTime = OffsetDateTime.of(2022, 8, 1, 0, 0, 0,0, ZoneOffset.of("+00:00"));
        final OffsetDateTime toDateTime = OffsetDateTime.of(2022, 8, 2, 0, 0, 0,0, ZoneOffset.of("+00:00"));
        requestInfo.pathParameters.put("fromDateTime", fromDateTime);
        requestInfo.pathParameters.put("toDateTime", toDateTime);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("fromDateTime='2022-08-01T00%3A00Z'"));
        assertTrue(uriResult.toString().contains("toDateTime='2022-08-02T00%3A00Z'"));
    }

    @Test
    public void SetsPathParametersOfBooleanType()
    {

        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.GET;
        requestInfo.urlTemplate = "http://localhost/users{?%24count}";

        // Act
        var count = true;
        requestInfo.pathParameters.put("%24count", count);

        // Assert
        var uriResult = assertDoesNotThrow(() -> requestInfo.getUri());
        assertTrue(uriResult.toString().contains("count=true"));
    }
    @Test
    public void SetsParsableContent() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final SerializationWriterFactory factoryMock = mock(SerializationWriterFactory.class);
        when(factoryMock.getSerializationWriter(anyString())).thenReturn(writerMock);
        final RequestAdapter requestAdapterMock = mock(RequestAdapter.class);
        when(requestAdapterMock.getSerializationWriterFactory()).thenReturn(factoryMock);
        requestInfo.setContentFromParsable(requestAdapterMock, "application/json", new TestEntity());

        verify(writerMock, times(1)).writeObjectValue(any(), any(TestEntity.class));
        verify(writerMock, never()).writeCollectionOfObjectValues(anyString(), any(ArrayList.class));
    }
    @Test
    public void SetsParsableContentCollection() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final SerializationWriterFactory factoryMock = mock(SerializationWriterFactory.class);
        when(factoryMock.getSerializationWriter(anyString())).thenReturn(writerMock);
        final RequestAdapter requestAdapterMock = mock(RequestAdapter.class);
        when(requestAdapterMock.getSerializationWriterFactory()).thenReturn(factoryMock);
        requestInfo.setContentFromParsable(requestAdapterMock, "application/json", new TestEntity[] {new TestEntity() });

        verify(writerMock, never()).writeObjectValue(any(), any(TestEntity.class));
        verify(writerMock, times(1)).writeCollectionOfObjectValues(any(), any(Iterable.class));
    }
    @Test
    public void SetsScalarContentCollection() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final SerializationWriterFactory factoryMock = mock(SerializationWriterFactory.class);
        when(factoryMock.getSerializationWriter(anyString())).thenReturn(writerMock);
        final RequestAdapter requestAdapterMock = mock(RequestAdapter.class);
        when(requestAdapterMock.getSerializationWriterFactory()).thenReturn(factoryMock);
        requestInfo.setContentFromScalarCollection(requestAdapterMock, "application/json", new String[] {"foo"});

        verify(writerMock, never()).writeStringValue(any(), anyString());
        verify(writerMock, times(1)).writeCollectionOfPrimitiveValues(any(), any(Iterable.class));
    }
    @Test
    public void SetsScalarContent() {
        // Arrange as the request builders would
        final RequestInformation requestInfo = new RequestInformation();
        requestInfo.httpMethod= HttpMethod.POST;
        requestInfo.urlTemplate = "http://localhost/users";
        final SerializationWriter writerMock = mock(SerializationWriter.class);
        final SerializationWriterFactory factoryMock = mock(SerializationWriterFactory.class);
        when(factoryMock.getSerializationWriter(anyString())).thenReturn(writerMock);
        final RequestAdapter requestAdapterMock = mock(RequestAdapter.class);
        when(requestAdapterMock.getSerializationWriterFactory()).thenReturn(factoryMock);
        requestInfo.setContentFromScalar(requestAdapterMock, "application/json", "foo");

        verify(writerMock, times(1)).writeStringValue(any(), anyString());
        verify(writerMock, never()).writeCollectionOfPrimitiveValues(any(), any(Iterable.class));
    }
}
