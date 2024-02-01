package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AllowedHostValidatorTest {

    @Test
    public void throwsExceptionForHttpOrHttpsHosts() {
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
}
