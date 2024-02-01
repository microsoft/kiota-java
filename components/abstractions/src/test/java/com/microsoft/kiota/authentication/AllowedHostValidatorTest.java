package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

class AllowedHostValidatorTest {

    @Test
    void throwsExceptionForHttpOrHttpsHosts() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new AllowedHostsValidator(
                                "graph.microsoft.com", "https://graph.microsoft.com"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new AllowedHostsValidator(
                                "http://graph.microsoft.com", "graph.microsoft.com"));
    }

    @Test
    void initialisesAllowedHostsSuccessfully() throws URISyntaxException {
        final AllowedHostsValidator validator =
                new AllowedHostsValidator(
                        "graph.microsoft.com", "graph.MICROSOFT.US ", "canary.graph.microsoft.com");
        assertEquals(3, validator.getAllowedHosts().size());
        assertTrue(validator.getAllowedHosts().contains("graph.microsoft.us"));
        assertTrue(validator.isUrlHostValid(new URI("https://graph.microsoft.com/v1/me")));
    }
}
