package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SerializationWriterFactoryTests {
    private static final MultipartSerializationWriterFactory _serializationWriterFactory =
            new MultipartSerializationWriterFactory();
    private static final String contentType = "multipart/form-data";

    @Test
    public void getsWriterForMultipartContentType() {
        final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(contentType);
        assertNotNull(serializationWriter);
    }

    @Test
    public void throwsArgumentOutOfRangeExceptionForInvalidContentType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter("application/json"));
    }

    @Test
    public void throwsArgumentNullExceptionForNoContentType() {
        assertThrows(
                NullPointerException.class,
                () -> _serializationWriterFactory.getSerializationWriter(""));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "multipart/form-data; boundary=abc",
                "multipart/form-data; charset=utf-8",
                "MULTIPART/FORM-DATA",
                "MULTIPART/FORM-DATA; BOUNDARY=xyz",
                "Multipart/Form-Data",
            })
    public void getsWriterForContentTypeWithParametersOrCaseVariants(final String inputType) {
        final var serializationWriter =
                _serializationWriterFactory.getSerializationWriter(inputType);
        assertNotNull(serializationWriter);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ";",
                ";;",
                "; boundary=abc",
                "   ; boundary=abc",
            })
    public void throwsIllegalArgumentExceptionForDelimiterOnlyOrMissingMediaType(
            final String inputType) {
        assertThrows(
                IllegalArgumentException.class,
                () -> _serializationWriterFactory.getSerializationWriter(inputType));
    }
}
