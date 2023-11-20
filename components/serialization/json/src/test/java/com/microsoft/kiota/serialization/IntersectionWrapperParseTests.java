package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.kiota.serialization.mocks.IntersectionTypeMock;
import com.microsoft.kiota.serialization.mocks.MyEnum;
import com.microsoft.kiota.serialization.mocks.SecondTestEntity;
import com.microsoft.kiota.serialization.mocks.TestEntity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class IntersectionWrapperParseTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final JsonSerializationWriterFactory _serializationWriterFactory =
            new JsonSerializationWriterFactory();
    private static final String contentType = "application/json";

    @Test
    void ParsesIntersectionTypeComplexProperty1() throws UnsupportedEncodingException {
        final var initialString =
                "{\"displayName\":\"McGill\",\"officeLocation\":\"Montreal\", \"id\": \"opaque\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result =
                parseNode.getObjectValue(IntersectionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNotNull(result.getComposedType1());
        assertNotNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals("opaque", result.getComposedType1().getId());
        assertEquals("McGill", result.getComposedType2().getDisplayName());
    }

    @Test
    void ParsesIntersectionTypeComplexProperty2() throws UnsupportedEncodingException {
        final var initialString =
                "{\"displayName\":\"McGill\",\"officeLocation\":\"Montreal\", \"id\": 10}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result =
                parseNode.getObjectValue(IntersectionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNotNull(result.getComposedType1());
        assertNotNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals(
                "10",
                result.getComposedType1()
                        .getId()); // difference in behaviour since Gson is more tolerant with
        // primitives
        assertNull(
                result.getComposedType2()
                        .getId()); // expected since multiple fields are mapped to the same value
        // and the first one wins
        assertEquals("McGill", result.getComposedType2().getDisplayName());
    }

    @Test
    void ParsesIntersectionTypeComplexProperty3() throws UnsupportedEncodingException {
        final var initialString =
                "[{\"@odata.type\":\"#microsoft.graph.TestEntity\",\"officeLocation\":\"Ottawa\","
                    + " \"id\": \"11\",\"myEnum\": \"VALUE2\"},"
                    + " {\"@odata.type\":\"#microsoft.graph.TestEntity\",\"officeLocation\":\"Montreal\","
                    + " \"id\": \"10\",\"myEnum\": \"VALUE1\"}]";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result =
                parseNode.getObjectValue(IntersectionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNull(result.getComposedType1());
        assertNull(result.getComposedType2());
        assertNotNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals(2, result.getComposedType3().size());
        assertEquals("Ottawa", result.getComposedType3().get(0).getOfficeLocation());
        assertEquals(MyEnum.MY_VALUE2, result.getComposedType3().get(0).getMyEnum());
        assertEquals(MyEnum.MY_VALUE1, result.getComposedType3().get(1).getMyEnum());
    }

    @Test
    void ParsesIntersectionTypeStringValue() throws UnsupportedEncodingException {
        final var initialString = "\"officeLocation\"";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result =
                parseNode.getObjectValue(IntersectionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNull(result.getComposedType1());
        assertNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNotNull(result.getStringValue());
        assertEquals("officeLocation", result.getStringValue());
    }

    @Test
    void SerializesIntersectionTypeStringValue() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new IntersectionTypeMock() {
                        {
                            setStringValue("officeLocation");
                        }
                    };

            writer.writeObjectValue("", model);
            try (final var result = writer.getSerializedContent()) {
                final String text = new String(result.readAllBytes(), StandardCharsets.UTF_8);
                assertEquals("\"officeLocation\"", text);
            }
        }
    }

    @Test
    void SerializesIntersectionTypeComplexProperty1() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new IntersectionTypeMock() {
                        {
                            setComposedType1(
                                    new TestEntity() {
                                        {
                                            setOfficeLocation("Montreal");
                                            setId("opaque");
                                        }
                                    });
                            setComposedType2(
                                    new SecondTestEntity() {
                                        {
                                            setDisplayName("McGill");
                                        }
                                    });
                        }
                    };

            writer.writeObjectValue("", model);
            try (final var result = writer.getSerializedContent()) {
                final String text = new String(result.readAllBytes(), StandardCharsets.UTF_8);
                assertEquals(
                        "{\"id\":\"opaque\",\"officeLocation\":\"Montreal\",\"displayName\":\"McGill\"}",
                        text);
            }
        }
    }

    @Test
    void SerializesIntersectionTypeComplexProperty2() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new IntersectionTypeMock() {
                        {
                            setComposedType2(
                                    new SecondTestEntity() {
                                        {
                                            setDisplayName("McGill");
                                            setId(10);
                                        }
                                    });
                        }
                    };

            writer.writeObjectValue("", model);
            try (final var result = writer.getSerializedContent()) {
                final String text = new String(result.readAllBytes(), StandardCharsets.UTF_8);
                assertEquals("{\"displayName\":\"McGill\",\"id\":10}", text);
            }
        }
    }

    @Test
    void SerializesIntersectionTypeComplexProperty3() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new IntersectionTypeMock() {
                        {
                            setComposedType3(
                                    new ArrayList<>() {
                                        {
                                            add(
                                                    new TestEntity() {
                                                        {
                                                            setOfficeLocation("Montreal");
                                                            setId("10");
                                                            setMyEnum(MyEnum.MY_VALUE2);
                                                        }
                                                    });
                                            add(
                                                    new TestEntity() {
                                                        {
                                                            setOfficeLocation("Ottawa");
                                                            setId("11");
                                                            setMyEnum(MyEnum.MY_VALUE1);
                                                        }
                                                    });
                                        }
                                    });
                        }
                    };

            writer.writeObjectValue("", model);
            try (final var result = writer.getSerializedContent()) {
                final String text = new String(result.readAllBytes(), StandardCharsets.UTF_8);
                assertEquals(
                        "[{\"id\":\"10\",\"officeLocation\":\"Montreal\",\"myEnum\":\"VALUE2\"},{\"id\":\"11\",\"officeLocation\":\"Ottawa\",\"myEnum\":\"VALUE1\"}]",
                        text);
            }
        }
    }
}
