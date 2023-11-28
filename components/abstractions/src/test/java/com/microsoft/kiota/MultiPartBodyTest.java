package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.microsoft.kiota.serialization.SerializationWriter;

import org.junit.jupiter.api.Test;

class MultiPartBodyTest {
    @Test
    void defensive() {
        final MultipartBody multipartBody = new MultipartBody();
        assertThrows(
                IllegalArgumentException.class,
                () -> multipartBody.addOrReplacePart(null, "foo", "bar"));
        assertThrows(
                IllegalArgumentException.class,
                () -> multipartBody.addOrReplacePart("foo", null, "bar"));
        assertThrows(
                NullPointerException.class,
                () -> multipartBody.addOrReplacePart("foo", "bar", null));
        assertThrows(IllegalArgumentException.class, () -> multipartBody.getPartValue(null));
        assertThrows(IllegalArgumentException.class, () -> multipartBody.removePart(null));
        assertThrows(NullPointerException.class, () -> multipartBody.serialize(null));
        assertThrows(
                UnsupportedOperationException.class, () -> multipartBody.getFieldDeserializers());
    }

    @Test
    void requiresRequestAdapter() {
        final MultipartBody multipartBody = new MultipartBody();
        final SerializationWriter writer = mock(SerializationWriter.class);
        assertThrows(IllegalStateException.class, () -> multipartBody.serialize(writer));
    }

    @Test
    void requiresPartsForSerialization() {
        final MultipartBody multipartBody = new MultipartBody();
        final SerializationWriter writer = mock(SerializationWriter.class);
        final RequestAdapter requestAdapter = mock(RequestAdapter.class);
        multipartBody.requestAdapter = requestAdapter;
        assertThrows(IllegalStateException.class, () -> multipartBody.serialize(writer));
    }

    @Test
    void addsPart() {
        final MultipartBody multipartBody = new MultipartBody();
        final RequestAdapter requestAdapter = mock(RequestAdapter.class);
        multipartBody.requestAdapter = requestAdapter;
        multipartBody.addOrReplacePart("foo", "bar", "baz");
        final Object result = multipartBody.getPartValue("foo");
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    void removesPart() {
        final MultipartBody multipartBody = new MultipartBody();
        final RequestAdapter requestAdapter = mock(RequestAdapter.class);
        multipartBody.requestAdapter = requestAdapter;
        multipartBody.addOrReplacePart("foo", "bar", "baz");
        multipartBody.removePart("FOO");
        final Object result = multipartBody.getPartValue("foo");
        assertNull(result);
    }
    // serialize method is being tested in the serialization library
}
