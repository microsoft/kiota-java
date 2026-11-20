package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.ClientException;
import com.microsoft.kiota.Compatibility;
import com.microsoft.kiota.store.BackingStoreSerializationWriterProxyFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class FactoryRegistryExceptionTest {
    @Test
    void unsupportedResponseContentTypeThrowsClientException() {
        var registry = new ParseNodeFactoryRegistry();
        var response =
                new ByteArrayInputStream(
                        "<html>Unavailable</html>".getBytes(StandardCharsets.UTF_8));
        var error =
                assertThrows(
                        ClientException.class,
                        () -> registry.getParseNode("text/html; charset=utf-8", response));
        assertEquals(
                "Content type text/html does not have a factory to be parsed", error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void unsupportedRequestContentTypeThrowsClientException(boolean changedValuesOnly) {
        var registry = new SerializationWriterFactoryRegistry();
        var error =
                assertThrows(
                        ClientException.class,
                        () -> registry.getSerializationWriter("text/html", changedValuesOnly));
        assertEquals(
                "Content type text/html does not have a factory to be serialized",
                error.getMessage());
    }

    @Test
    void invalidArgumentsKeepTheirValidationExceptions() {
        var parser = new ParseNodeFactoryRegistry();
        var writer = new SerializationWriterFactoryRegistry();
        assertThrows(
                NullPointerException.class,
                () -> parser.getParseNode(null, new ByteArrayInputStream(new byte[0])));
        assertThrows(
                NullPointerException.class,
                () -> parser.getParseNode("", new ByteArrayInputStream(new byte[0])));
        assertThrows(NullPointerException.class, () -> parser.getParseNode("text/html", null));
        assertThrows(NullPointerException.class, () -> writer.getSerializationWriter(null));
        assertThrows(NullPointerException.class, () -> writer.getSerializationWriter(""));
    }

    @Test
    void registeredVendorContentTypesStillWork() throws Exception {
        var parser = new ParseNodeFactoryRegistry();
        parser.contentTypeAssociatedFactories.put("application/json", new JsonParseNodeFactory());
        var response = new ByteArrayInputStream("42".getBytes(StandardCharsets.UTF_8));
        assertEquals(
                42,
                parser.getParseNode("application/vnd.example+json; charset=utf-8", response)
                        .getIntegerValue());
        var writers = new SerializationWriterFactoryRegistry();
        writers.contentTypeAssociatedFactories.put(
                "application/json", new JsonSerializationWriterFactory());
        try (var writer = writers.getSerializationWriter("application/vnd.example+json")) {
            writer.writeIntegerValue(null, 42);
            assertEquals(
                    "42",
                    new String(
                            Compatibility.readAllBytes(writer.getSerializedContent()),
                            StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "APPLICATION/JSON",
                "Application/Json; charset=utf-8",
                "application/json ; charset=utf-8",
                "  application/json ; charset=utf-8",
                "Application/Vnd.Example+JSON ; charset=utf-8",
            })
    void parseNodeRegistryResolvesCaseInsensitiveAndWhitespacePaddedContentTypes(
            final String inputContentType) {
        var parser = new ParseNodeFactoryRegistry();
        parser.contentTypeAssociatedFactories.put("application/json", new JsonParseNodeFactory());
        var response = new ByteArrayInputStream("42".getBytes(StandardCharsets.UTF_8));
        assertEquals(42, parser.getParseNode(inputContentType, response).getIntegerValue());
    }

    @Test
    void parseNodeRegistryMatchesMixedCaseRegisteredFactoryKey() {
        var parser = new ParseNodeFactoryRegistry();
        var customFactory = mock(ParseNodeFactory.class);
        var mockNode = mock(ParseNode.class);
        when(customFactory.getParseNode(eq("application/custom"), any())).thenReturn(mockNode);
        parser.contentTypeAssociatedFactories.put("Application/Custom", customFactory);

        var response = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        var result = parser.getParseNode("application/custom ; version=1", response);
        assertNotNull(result);
        verify(customFactory).getParseNode(eq("application/custom"), eq(response));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ";",
                ";;",
                "; charset=utf-8",
                "   ",
                "   ; charset=utf-8",
            })
    void parseNodeRegistryThrowsClientExceptionForDelimiterOnlyOrBlankContentTypes(
            final String inputContentType) {
        var parser = new ParseNodeFactoryRegistry();
        parser.contentTypeAssociatedFactories.put("application/json", new JsonParseNodeFactory());
        var response = new ByteArrayInputStream("42".getBytes(StandardCharsets.UTF_8));
        assertThrows(ClientException.class, () -> parser.getParseNode(inputContentType, response));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "APPLICATION/JSON",
                "Application/Json; charset=utf-8",
                "application/json ; charset=utf-8",
                "  application/json ; charset=utf-8",
                "Application/Vnd.Example+JSON ; charset=utf-8",
            })
    void serializationWriterRegistryResolvesCaseInsensitiveAndWhitespacePaddedContentTypes(
            final String inputContentType) throws Exception {
        var writers = new SerializationWriterFactoryRegistry();
        writers.contentTypeAssociatedFactories.put(
                "application/json", new JsonSerializationWriterFactory());
        try (var writer = writers.getSerializationWriter(inputContentType)) {
            writer.writeIntegerValue(null, 42);
            assertEquals(
                    "42",
                    new String(
                            Compatibility.readAllBytes(writer.getSerializedContent()),
                            StandardCharsets.UTF_8));
        }
    }

    @Test
    void serializationWriterRegistryMatchesMixedCaseKeyAndBackingStoreBypass() throws Exception {
        var writers = new SerializationWriterFactoryRegistry();
        var customFactory = mock(SerializationWriterFactory.class);
        var mockWriter = mock(SerializationWriter.class);
        when(customFactory.getSerializationWriter("application/custom")).thenReturn(mockWriter);
        writers.contentTypeAssociatedFactories.put("Application/Custom", customFactory);

        var result = writers.getSerializationWriter("application/custom ; version=1");
        assertNotNull(result);
        verify(customFactory).getSerializationWriter("application/custom");

        var backingStoreRegistry = new SerializationWriterFactoryRegistry();
        backingStoreRegistry.contentTypeAssociatedFactories.put(
                "application/json",
                new BackingStoreSerializationWriterProxyFactory(
                        new JsonSerializationWriterFactory()));
        try (var writer =
                backingStoreRegistry.getSerializationWriter(
                        "APPLICATION/JSON ; charset=utf-8", false)) {
            assertNotNull(writer);
            writer.writeIntegerValue(null, 7);
            assertEquals(
                    "7",
                    new String(
                            Compatibility.readAllBytes(writer.getSerializedContent()),
                            StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ";",
                ";;",
                "; charset=utf-8",
                "   ",
                "   ; charset=utf-8",
            })
    void serializationWriterRegistryThrowsClientExceptionForDelimiterOnlyOrBlankContentTypes(
            final String inputContentType) {
        var writers = new SerializationWriterFactoryRegistry();
        writers.contentTypeAssociatedFactories.put(
                "application/json", new JsonSerializationWriterFactory());
        assertThrows(ClientException.class, () -> writers.getSerializationWriter(inputContentType));
    }

    @Test
    void caseInsensitiveMapNormalizesKeysOnAddingAndLookup() {
        var map = new CaseInsensitiveMap<String>();
        map.put("APPLICATION/JSON", "json");
        map.put("Text/Plain ; Charset=UTF-8", "text");

        assertTrue(map.containsKey("application/json"));
        assertTrue(map.containsKey("APPLICATION/JSON"));
        assertEquals("json", map.get("application/json"));
        assertEquals("json", map.get("Application/Json"));
        assertEquals("json", map.get("APPLICATION/JSON ; charset=utf-8"));

        assertTrue(map.containsKey("text/plain"));
        assertEquals("text", map.get("TEXT/PLAIN"));

        map.putAll(java.util.Collections.singletonMap("IMAGE/PNG", "png"));
        assertTrue(map.containsKey("image/png"));
        assertEquals("png", map.get("IMAGE/PNG"));

        assertEquals("default", map.getOrDefault("UNKNOWN/TYPE", "default"));
        map.putIfAbsent("APPLICATION/XML", "xml");
        assertTrue(map.containsKey("application/xml"));
        assertEquals("xml", map.remove("Application/XML"));
        assertFalse(map.containsKey("application/xml"));
    }
}
