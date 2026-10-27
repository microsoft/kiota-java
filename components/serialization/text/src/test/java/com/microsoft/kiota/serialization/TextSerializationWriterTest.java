package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

public class TextSerializationWriterTest {

    private String readStream(final TextSerializationWriter writer) {
        try (final var is = writer.getSerializedContent();
                final var isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                final var reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testWritesStringValue() {
        try (final var writer = new TextSerializationWriter()) {
            writer.writeStringValue(null, "hello world");
            assertEquals("hello world", readStream(writer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testWritesBooleanValue() {
        try (final var writer = new TextSerializationWriter()) {
            writer.writeBooleanValue(null, true);
            assertEquals("true", readStream(writer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testWritesIntegerValue() {
        try (final var writer = new TextSerializationWriter()) {
            writer.writeIntegerValue(null, 42);
            assertEquals("42", readStream(writer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testWritesByteArrayValue() {
        try (final var writer = new TextSerializationWriter()) {
            writer.writeByteArrayValue(null, "test".getBytes(StandardCharsets.UTF_8));
            assertEquals("dGVzdA==", readStream(writer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testThrowsWhenKeyIsProvided() {
        try (final var writer = new TextSerializationWriter()) {
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.writeStringValue("key", "value"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testThrowsWhenWritingMultipleValues() {
        try (final var writer = new TextSerializationWriter()) {
            writer.writeStringValue(null, "first");
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.writeStringValue(null, "second"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void testThrowsOnStructuredDataCalls() {
        try (final var writer = new TextSerializationWriter()) {
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.writeCollectionOfPrimitiveValues(null, Collections.emptyList()));
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.writeAdditionalData(Collections.emptyMap()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
