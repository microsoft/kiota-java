package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SerializationWriterFactoryTests {
    private static final FormSerializationWriterFactory _serializationWriterFactory =
            new FormSerializationWriterFactory();
    private static final String contentType = "application/x-www-form-urlencoded";

    @Test
    public void getsWriterForFormContentType() {
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
                "application/x-www-form-urlencoded; charset=utf-8",
                "application/x-www-form-urlencoded; charset=UTF-8",
                "application/x-www-form-urlencoded; charset=us-ascii",
                "APPLICATION/X-WWW-FORM-URLENCODED",
                "APPLICATION/X-WWW-FORM-URLENCODED; CHARSET=UTF-8",
                "Application/x-www-form-urlencoded",
                "Application/x-www-form-urlencoded; charset=utf-8",
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
