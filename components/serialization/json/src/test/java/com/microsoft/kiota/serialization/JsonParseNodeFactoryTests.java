package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

class JsonParseNodeFactoryTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final String contentType = "application/json";

    private static <T> T nullValue() {
        return Collections.<String, T>emptyMap().get("missing");
    }

    @Test
    void getsParseNodeForJsonContentType() {
        final var rawResponse =
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        assertNotNull(parseNode);
        final var child = parseNode.getChildNode("key");
        assertNotNull(child);
        assertEquals("value", child.getStringValue());
    }

    @Test
    void throwsIllegalArgumentExceptionForInvalidContentType() {
        final var rawResponse =
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(
                IllegalArgumentException.class,
                () -> _parseNodeFactory.getParseNode("text/plain", rawResponse));
    }

    @Test
    void throwsNullPointerExceptionForEmptyOrNullArguments() {
        final var rawResponse =
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(
                NullPointerException.class, () -> _parseNodeFactory.getParseNode("", rawResponse));
        assertThrows(
                NullPointerException.class,
                () -> _parseNodeFactory.getParseNode(nullValue(), rawResponse));
        assertThrows(
                NullPointerException.class,
                () -> _parseNodeFactory.getParseNode(contentType, nullValue()));
        assertThrows(NullPointerException.class, () -> new JsonParseNodeFactory(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "application/json; charset=utf-8",
                "application/json; charset=UTF-8",
                "application/json ; charset=utf-8",
                "application/json; odata.metadata=minimal",
                "APPLICATION/JSON",
                "APPLICATION/JSON; CHARSET=UTF-8",
                "Application/Json",
                "  application/json ; charset=utf-8",
            })
    void getsParseNodeForContentTypeWithParametersOrCaseVariants(final String inputType) {
        final var rawResponse =
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        final var parseNode = _parseNodeFactory.getParseNode(inputType, rawResponse);
        assertNotNull(parseNode);
        final var child = parseNode.getChildNode("key");
        assertNotNull(child);
        assertEquals("value", child.getStringValue());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ";",
                ";;",
                "; charset=utf-8",
                "   ; charset=utf-8",
            })
    void throwsIllegalArgumentExceptionForDelimiterOnlyOrMissingMediaType(final String inputType) {
        final var rawResponse =
                new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(
                IllegalArgumentException.class,
                () -> _parseNodeFactory.getParseNode(inputType, rawResponse));
    }
}
