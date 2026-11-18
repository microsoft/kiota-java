package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.kiota.Compatibility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

class JsonSerializationWriterFactoryTests {
    private static final JsonSerializationWriterFactory _serializationWriterFactory =
            new JsonSerializationWriterFactory();
    private static final String contentType = "application/json";

    private static <T> T nullValue() {
        return Collections.<String, T>emptyMap().get("missing");
    }

    @Test
    void getsWriterForJsonContentType() throws Exception {
        try (final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(contentType)) {
            assertNotNull(serializationWriter);
            serializationWriter.writeStringValue(null, "ok");
            assertEquals(
                    "\"ok\"",
                    new String(
                            Compatibility.readAllBytes(serializationWriter.getSerializedContent()),
                            StandardCharsets.UTF_8));
        }
    }

    @Test
    void throwsIllegalArgumentExceptionForInvalidContentType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter("text/plain"));
    }

    @Test
    void throwsNullPointerExceptionForEmptyOrNullArguments() {
        assertThrows(
                NullPointerException.class,
                () -> _serializationWriterFactory.getSerializationWriter(""));
        assertThrows(
                NullPointerException.class,
                () -> _serializationWriterFactory.getSerializationWriter(nullValue()));
        final var gsonError =
                assertThrows(
                        NullPointerException.class,
                        () -> new JsonSerializationWriterFactory(nullValue()));
        assertTrue(gsonError.getMessage().contains("parameter gson cannot be null"));
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
    void getsWriterForContentTypeWithParametersOrCaseVariants(final String inputType)
            throws Exception {
        try (final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(inputType)) {
            assertNotNull(serializationWriter);
            assertEquals(contentType, _serializationWriterFactory.getValidContentType());
            serializationWriter.writeStringValue(null, "ok");
            assertEquals(
                    "\"ok\"",
                    new String(
                            Compatibility.readAllBytes(serializationWriter.getSerializedContent()),
                            StandardCharsets.UTF_8));
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
    void throwsIllegalArgumentExceptionForDelimiterOnlyOrMissingMediaType(final String inputType) {
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter(inputType));
    }
}
