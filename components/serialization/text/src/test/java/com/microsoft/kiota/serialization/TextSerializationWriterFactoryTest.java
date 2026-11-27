package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TextSerializationWriterFactoryTest {
    private static final TextSerializationWriterFactory _serializationWriterFactory =
            new TextSerializationWriterFactory();
    private static final String contentType = "text/plain";

    private String readStream(final SerializationWriter writer) {
        try (final var is = writer.getSerializedContent();
                final var isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                final var reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void getsValidContentType() {
        assertEquals("text/plain", _serializationWriterFactory.getValidContentType());
    }

    @Test
    public void getsWriterForTextContentType() {
        try (final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(contentType)) {
            assertNotNull(serializationWriter);
            serializationWriter.writeStringValue(null, "hello world");
            assertEquals("hello world", readStream(serializationWriter));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void throwsIllegalArgumentExceptionForInvalidContentType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter("application/json"));
    }

    @Test
    public void throwsNullPointerExceptionForNoContentType() {
        assertThrows(
                NullPointerException.class,
                () -> _serializationWriterFactory.getSerializationWriter(""));
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
    public void getsWriterForContentTypeWithParametersOrCaseVariants(final String inputType) {
        try (final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(inputType)) {
            assertNotNull(serializationWriter);
            serializationWriter.writeStringValue(null, "hello world");
            assertEquals("hello world", readStream(serializationWriter));
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
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter(inputType));
    }
}
