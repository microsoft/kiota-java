package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeParseException;

class JsonParseNodeTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final String contentType = "application/json";

    @Test
    void itDDoesNotFailForGetChildElementOnMissingKey() throws UnsupportedEncodingException {
        final var initialString = "{displayName\": \"Microsoft Teams Meeting\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getChildNode("@odata.type");
        assertNull(result);
    }

    @Test
    void testParsesDateTimeOffset() {
        final var dateTimeOffsetString = "2024-02-12T19:47:39+02:00";
        final var jsonElement = JsonParser.parseString("\"" + dateTimeOffsetString + "\"");
        final var result = new JsonParseNode(jsonElement).getOffsetDateTimeValue();
        assertEquals(dateTimeOffsetString, result.toString());
    }

    @Test
    void testParsesDateTimeStringWithoutOffsetToDateTimeOffset() {
        final var dateTimeString = "2024-02-12T19:47:39";
        final var jsonElement = JsonParser.parseString("\"" + dateTimeString + "\"");
        final var result = new JsonParseNode(jsonElement).getOffsetDateTimeValue();
        assertEquals(dateTimeString + "Z", result.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-12T19:47:39 Europe/Paris", "19:47:39"})
    void testInvalidOffsetDateTimeStringThrowsException(final String dateTimeString) {
        final var jsonElement = JsonParser.parseString("\"" + dateTimeString + "\"");
        try {
            new JsonParseNode(jsonElement).getOffsetDateTimeValue();
        } catch (final Exception ex) {
            assertInstanceOf(DateTimeParseException.class, ex);
            assertTrue(ex.getMessage().contains(dateTimeString));
        }
    }
}
