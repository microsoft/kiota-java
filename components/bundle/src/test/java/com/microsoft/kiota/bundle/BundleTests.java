package com.microsoft.kiota.bundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.serialization.ParseNodeFactoryRegistry;
import com.microsoft.kiota.serialization.SerializationWriterFactoryRegistry;

import org.junit.jupiter.api.Test;

public class BundleTests {
    @Test
    void throwsErrorNullAuthenticationProvider() throws Exception {
        var exception =
                assertThrows(NullPointerException.class, () -> new DefaultRequestAdapter(null));
        assertEquals("parameter authenticationProvider cannot be null", exception.getMessage());
    }

    @Test
    void serializersAreRegisteredAsExpected() throws Exception {
        final var authenticationProviderMock = mock(AuthenticationProvider.class);
        var defaultRequestAdapter = new DefaultRequestAdapter(authenticationProviderMock);
        assertEquals("", defaultRequestAdapter.getBaseUrl());

        // validate
        var serializerCount =
                SerializationWriterFactoryRegistry.defaultInstance.contentTypeAssociatedFactories
                        .size();
        var deserializerCount =
                ParseNodeFactoryRegistry.defaultInstance.contentTypeAssociatedFactories.size();

        assertEquals(4, serializerCount); // four serializers present
        assertEquals(3, deserializerCount); // three deserializers present

        var serializerKeys =
                SerializationWriterFactoryRegistry.defaultInstance.contentTypeAssociatedFactories
                        .keySet();
        var deserializerKeys =
                ParseNodeFactoryRegistry.defaultInstance.contentTypeAssociatedFactories.keySet();

        assertTrue(serializerKeys.contains("application/json"));
        assertTrue(
                deserializerKeys.contains(
                        "application/json")); // Serializer and deserializer present for
        // application/json

        assertTrue(serializerKeys.contains("text/plain"));
        assertTrue(
                deserializerKeys.contains(
                        "text/plain")); // Serializer and deserializer present for text/plain

        assertTrue(serializerKeys.contains("application/x-www-form-urlencoded"));
        assertTrue(
                deserializerKeys.contains(
                        "application/x-www-form-urlencoded")); // Serializer and deserializer
        // present for
        // application/x-www-form-urlencoded

        assertTrue(
                serializerKeys.contains(
                        "multipart/form-data")); // Serializer present for multipart/form-data
    }
}
