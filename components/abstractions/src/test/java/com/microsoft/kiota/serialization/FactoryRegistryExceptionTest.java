package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.microsoft.kiota.ClientException;

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
                            writer.getSerializedContent().readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
