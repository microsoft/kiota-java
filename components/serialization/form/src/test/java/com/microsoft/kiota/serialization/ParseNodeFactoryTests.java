package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class ParseNodeFactoryTests {
    private static final FormParseNodeFactory _parseNodeFactory = new FormParseNodeFactory();
    private static final String contentType = "application/x-www-form-urlencoded";

    @Test
    public void getsWriterForFormContentType() throws UnsupportedEncodingException {
        final var initialString = "key1=value1&key2=value2";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        assertNotNull(parseNode);
    }

    @Test
    public void throwsArgumentOutOfRangeExceptionForInvalidContentType()
            throws UnsupportedEncodingException {
        final var initialString = "key1=value1&key2=value2";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        assertThrows(
                IllegalArgumentException.class,
                () -> _parseNodeFactory.getParseNode("application/json", rawResponse));
    }

    @Test
    public void throwsArgumentNullExceptionForNoContentType() throws UnsupportedEncodingException {
        final var initialString = "key1=value1&key2=value2";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        assertThrows(
                NullPointerException.class, () -> _parseNodeFactory.getParseNode("", rawResponse));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "application/x-www-form-urlencoded; charset=utf-8",
                "application/x-www-form-urlencoded; charset=UTF-8",
                "application/x-www-form-urlencoded; charset=us-ascii",
                "APPLICATION/X-WWW-FORM-URLENCODED",
                "APPLICATION/X-WWW-FORM-URLENCODED; CHARSET=UTF-8",
                "Application/x-www-form-urlencoded",
                "Application/x-www-form-urlencoded; charset=utf-8",
            })
    public void getsParseNodeForContentTypeWithParametersOrCaseVariants(final String inputType)
            throws UnsupportedEncodingException {
        final var initialString = "key1=value1&key2=value2";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(inputType, rawResponse);
        assertNotNull(parseNode);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ";",
                ";;",
                "; charset=utf-8",
                "   ; charset=utf-8",
            })
    public void throwsIllegalArgumentExceptionForDelimiterOnlyOrMissingMediaType(
            final String inputType) throws UnsupportedEncodingException {
        final var initialString = "key1=value1&key2=value2";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        assertThrows(
                IllegalArgumentException.class,
                () -> _parseNodeFactory.getParseNode(inputType, rawResponse));
    }
}
