package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.kiota.RequestInformation;
import java.net.URISyntaxException;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class ApiKeyAuthenticationProviderTest {
    @Test
    void DefensivePrograming() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ApiKeyAuthenticationProvider(
                                "", "paramName", ApiKeyLocation.QUERY_PARAMETER));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ApiKeyAuthenticationProvider("key", "", ApiKeyLocation.QUERY_PARAMETER));

        var value =
                new ApiKeyAuthenticationProvider("key", "param", ApiKeyLocation.QUERY_PARAMETER);
        assertThrows(NullPointerException.class, () -> value.authenticateRequest(null, null));
    }

    @Test
    void AddsInQueryParameter() throws IllegalStateException, URISyntaxException {
        var value =
                new ApiKeyAuthenticationProvider("key", "param", ApiKeyLocation.QUERY_PARAMETER);
        var request =
                new RequestInformation() {
                    {
                        urlTemplate = "https://localhost{?param1}";
                    }
                };
        value.authenticateRequest(request, null);
        assertEquals("https://localhost?param=key", request.getUri().toString());
    }

    @Test
    void AddsInQueryParameterWithOtherParameters()
            throws IllegalStateException, URISyntaxException {
        var value =
                new ApiKeyAuthenticationProvider("key", "param", ApiKeyLocation.QUERY_PARAMETER);
        var request =
                new RequestInformation() {
                    {
                        urlTemplate = "https://localhost{?param1}";
                    }
                };
        request.addQueryParameter("param1", "value1");
        value.authenticateRequest(request, null);
        assertNull(request.headers.get("param"));
        assertEquals("https://localhost?param1=value1&param=key", request.getUri().toString());
    }

    @Test
    void AddsInHeaders() throws IllegalStateException, URISyntaxException {
        var value = new ApiKeyAuthenticationProvider("key", "param", ApiKeyLocation.HEADER);
        var request =
                new RequestInformation() {
                    {
                        urlTemplate = "https://localhost{?param1}";
                    }
                };
        value.authenticateRequest(request, null);
        assertEquals(
                new HashSet<String>() {
                    {
                        add("key");
                    }
                },
                request.headers.get("param"));
        assertEquals("https://localhost", request.getUri().toString());
    }
}
