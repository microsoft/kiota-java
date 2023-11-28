package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.kiota.serialization.mocks.SecondTestEntity;
import com.microsoft.kiota.serialization.mocks.TestEntity;
import com.microsoft.kiota.serialization.mocks.UnionTypeMock;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class UnionWrapperParseTests {
    private static final JsonParseNodeFactory _parseNodeFactory = new JsonParseNodeFactory();
    private static final JsonSerializationWriterFactory _serializationWriterFactory =
            new JsonSerializationWriterFactory();
    private static final String contentType = "application/json";

    @Test
    void ParsesUnionTypeComplexProperty1() throws UnsupportedEncodingException {
        final var initialString =
                "{\"@odata.type\":\"#microsoft.graph.testEntity\",\"officeLocation\":\"Montreal\","
                        + " \"id\": \"opaque\"}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getObjectValue(UnionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNotNull(result.getComposedType1());
        assertNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals("Montreal", result.getComposedType1().getOfficeLocation());
        assertEquals("opaque", result.getComposedType1().getId());
    }

    @Test
    void ParsesUnionTypeComplexProperty2() throws UnsupportedEncodingException {
        final var initialString =
                "{\"@odata.type\":\"#microsoft.graph.secondTestEntity\",\"officeLocation\":\"Montreal\","
                    + " \"id\": 10}";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getObjectValue(UnionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNull(result.getComposedType1());
        assertNotNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals(10, result.getComposedType2().getId());
    }

    @Test
    void ParsesUnionTypeComplexProperty3() throws UnsupportedEncodingException {
        final var initialString =
                "[{\"@odata.type\":\"#microsoft.graph.TestEntity\",\"officeLocation\":\"Ottawa\","
                    + " \"id\": \"11\"},"
                    + " {\"@odata.type\":\"#microsoft.graph.TestEntity\",\"officeLocation\":\"Montreal\","
                    + " \"id\": \"10\"}]";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getObjectValue(UnionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNull(result.getComposedType1());
        assertNull(result.getComposedType2());
        assertNotNull(result.getComposedType3());
        assertNull(result.getStringValue());
        assertEquals(2, result.getComposedType3().size());
        assertEquals("11", result.getComposedType3().get(0).getId());
    }

    @Test
    void ParsesUnionTypeStringValue() throws UnsupportedEncodingException {
        final var initialString = "\"officeLocation\"";
        final var rawResponse = new ByteArrayInputStream(initialString.getBytes("UTF-8"));
        final var parseNode = _parseNodeFactory.getParseNode(contentType, rawResponse);
        final var result = parseNode.getObjectValue(UnionTypeMock::createFromDiscriminatorValue);
        assertNotNull(result);
        assertNull(result.getComposedType1());
        assertNull(result.getComposedType2());
        assertNull(result.getComposedType3());
        assertNotNull(result.getStringValue());
        assertEquals("officeLocation", result.getStringValue());
    }

    @Test
    void SerializesUnionTypeStringValue() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new UnionTypeMock() {
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
    void SerializesUnionTypeComplexProperty1() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new UnionTypeMock() {
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
                assertEquals("{\"id\":\"opaque\",\"officeLocation\":\"Montreal\"}", text);
            }
        }
    }

    @Test
    void SerializesUnionTypeComplexProperty2() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new UnionTypeMock() {
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
    void SerializesUnionTypeComplexProperty3() throws IOException {
        try (final var writer = _serializationWriterFactory.getSerializationWriter(contentType)) {
            var model =
                    new UnionTypeMock() {
                        {
                            setComposedType3(
                                    new ArrayList<>() {
                                        {
                                            add(
                                                    new TestEntity() {
                                                        {
                                                            setOfficeLocation("Montreal");
                                                            setId("10");
                                                        }
                                                    });
                                            add(
                                                    new TestEntity() {
                                                        {
                                                            setOfficeLocation("Ottawa");
                                                            setId("11");
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
                        "[{\"id\":\"10\",\"officeLocation\":\"Montreal\"},{\"id\":\"11\",\"officeLocation\":\"Ottawa\"}]",
                        text);
            }
        }
    }
}
