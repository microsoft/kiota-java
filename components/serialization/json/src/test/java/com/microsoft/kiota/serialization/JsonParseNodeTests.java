package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

class JsonParseNodeTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final JsonSerializationWriterFactory _serializationWriterFactory =
            new JsonSerializationWriterFactory();
    private static final String contentType = "application/json";

    @Test
    void ItDDoesNotFailForGetChildElementOnMissingKey() throws UnsupportedEncodingException {
        final var initialString = "{displayName\": \"Microsoft Teams Meeting\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getChildNode("@odata.type");
        assertNull(result);
    }
}
