package com.microsoft.kiota;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

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
}
