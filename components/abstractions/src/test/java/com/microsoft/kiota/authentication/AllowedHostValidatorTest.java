package com.microsoft.kiota.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

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

    @Test
    void returnsFalseForSubdomainMatchingExactHost() throws URISyntaxException {
        final AllowedHostsValidator validator = new AllowedHostsValidator("example.com");

        assertFalse(validator.isUrlHostValid(new URI("https://sub.example.com/path")));
    }

    @Test
    void returnsTrueForSubdomainMatchingAllowedSuffix() throws URISyntaxException {
        final AllowedHostsValidator validator = new AllowedHostsValidator(".fabric.microsoft.com");

        assertTrue(
                validator.isUrlHostValid(
                        new URI("https://abc.123.graphql.fabric.microsoft.com/path")));
    }

    @Test
    void returnsFalseForBareDomainWhenAllowedAsSuffix() throws URISyntaxException {
        final AllowedHostsValidator validator = new AllowedHostsValidator(".fabric.microsoft.com");

        assertFalse(validator.isUrlHostValid(new URI("https://fabric.microsoft.com/path")));
    }

    @Test
    void suffixHostMatchingIsCaseInsensitive() throws URISyntaxException {
        final AllowedHostsValidator validator = new AllowedHostsValidator(".Fabric.Microsoft.COM");

        assertTrue(
                validator.isUrlHostValid(
                        new URI("https://ABC.z2c.graphql.fabric.microsoft.com/path")));
    }

    @Test
    void allowsMultipleValidHosts() throws URISyntaxException {
        final AllowedHostsValidator validator =
                new AllowedHostsValidator(
                        "example.com", "api.example.com", ".fabric.microsoft.com");

        assertTrue(validator.isUrlHostValid(new URI("https://example.com/path")));
        assertTrue(validator.isUrlHostValid(new URI("https://api.example.com/path")));
        assertFalse(validator.isUrlHostValid(new URI("https://other.com/path")));
        assertTrue(
                validator.isUrlHostValid(
                        new URI("https://abc.123.graphql.fabric.microsoft.com/path")));
    }

    @Test
    void allowsSuffixBasedHostsAfterUpdate() throws URISyntaxException {
        final AllowedHostsValidator validator = new AllowedHostsValidator("example.com");

        validator.setAllowedHosts(Collections.singleton(".fabric.microsoft.com"));

        assertTrue(
                validator.isUrlHostValid(
                        new URI("https://abc.123.graphql.fabric.microsoft.com/path")));
    }
}
