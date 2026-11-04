package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TextParseNodeFactoryTest {
    private static final TextParseNodeFactory _parseNodeFactory = new TextParseNodeFactory();
    private static final String contentType = "text/plain";

    private InputStream createStream(final String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void getsValidContentType() {
        assertEquals("text/plain", _parseNodeFactory.getValidContentType());
    }

    @Test
    public void getsParseNodeForTextContentType() {
        try (final var stream = createStream("hello world")) {
            final var parseNode = _parseNodeFactory.getParseNode(contentType, stream);
            assertNotNull(parseNode);
            assertEquals("hello world", parseNode.getStringValue());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void throwsIllegalArgumentExceptionForInvalidContentType() {
        try (final var stream = createStream("hello world")) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> _parseNodeFactory.getParseNode("application/json", stream));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void throwsNullPointerExceptionForNoContentType() {
        try (final var stream = createStream("hello world")) {
            assertThrows(
                    NullPointerException.class, () -> _parseNodeFactory.getParseNode("", stream));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "text/plain; charset=utf-8",
                "text/plain; charset=UTF-8",
                "text/plain; charset=us-ascii",
                "TEXT/PLAIN",
                "TEXT/PLAIN; CHARSET=UTF-8",
                "Text/Plain",
                "Text/Plain; charset=utf-8",
            })
    public void getsParseNodeForContentTypeWithParametersOrCaseVariants(final String inputType) {
        try (final var stream = createStream("hello world")) {
            final var parseNode = _parseNodeFactory.getParseNode(inputType, stream);
            assertNotNull(parseNode);
            assertEquals("hello world", parseNode.getStringValue());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
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
            final String inputType) {
        try (final var stream = createStream("hello world")) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> _parseNodeFactory.getParseNode(inputType, stream));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
