package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.format.DateTimeParseException;

public class TextParseNodeTest {

    @Test
    void testParsesDateTimeOffset() {
        final var dateTimeOffsetString = "2024-02-12T19:47:39+02:00";
        final var result = new TextParseNode(dateTimeOffsetString).getOffsetDateTimeValue();
        assertEquals(dateTimeOffsetString, result.toString());
    }

    @Test
    void testParsesDateTimeStringWithoutOffsetToDateTimeOffset() {
        final var dateTimeString = "2024-02-12T19:47:39";
        final var result = new TextParseNode(dateTimeString).getOffsetDateTimeValue();
        assertEquals(dateTimeString + "Z", result.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-12T19:47:39 Europe/Paris", "19:47:39"})
    void testInvalidOffsetDateTimeStringThrowsException(final String dateTimeString) {
        try {
            new TextParseNode(dateTimeString).getOffsetDateTimeValue();
        } catch (final Exception ex) {
            assertInstanceOf(DateTimeParseException.class, ex);
            assertTrue(ex.getMessage().contains(dateTimeString));
        }
    }
}
