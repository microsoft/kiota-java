package com.microsoft.kiota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class RequestHeadersTest {
    @Test
    void Defensive() {
        final RequestHeaders requestHeaders = new RequestHeaders();
        assertEquals(0, requestHeaders.get(1).size());
        assertEquals(0, requestHeaders.remove(2).size());
        assertFalse(requestHeaders.containsKey(requestHeaders));
        requestHeaders.putAll(null);
    }

    @Test
    void Adds() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        assertTrue(requestHeaders.isEmpty());
        // Act
        requestHeaders.add("key", "value");
        // Assert
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
        assertTrue(requestHeaders.containsKey("key"));
        assertFalse(requestHeaders.isEmpty());
        assertEquals(1, requestHeaders.keySet().size());
        assertEquals(1, requestHeaders.values().size());

        requestHeaders.add("key", "value2");
        assertEquals(1, requestHeaders.size());
        assertEquals(2, requestHeaders.get("key").size());
    }

    @Test
    void TryAdds() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        assertTrue(requestHeaders.isEmpty());
        // Act
        var result = requestHeaders.tryAdd("key", "value");
        // Assert
        assertTrue(result);
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
        assertTrue(requestHeaders.containsKey("key"));
        assertFalse(requestHeaders.isEmpty());
        assertEquals(1, requestHeaders.keySet().size());
        assertEquals(1, requestHeaders.values().size());

        result = requestHeaders.tryAdd("key", "value2");
        assertFalse(result);
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
    }

    @Test
    void NormalizesKey() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        // Act
        requestHeaders.add("Key", "value");
        // Assert
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
    }

    @Test
    void Puts() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        assertTrue(requestHeaders.isEmpty());
        // Act
        requestHeaders.put(
                "key",
                new HashSet<String>() {
                    {
                        add("value");
                    }
                });
        // Assert
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
        assertTrue(requestHeaders.containsKey("key"));
        assertFalse(requestHeaders.isEmpty());
    }

    @Test
    void Removes() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.add("key", "value");
        assertEquals(1, requestHeaders.size());
        // Act
        requestHeaders.remove("key");
        // Assert
        assertEquals(0, requestHeaders.size());
        assertTrue(requestHeaders.isEmpty());
    }

    @Test
    void RemovesValue() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.add("key", "value");
        requestHeaders.add("key", "value2");
        assertEquals(1, requestHeaders.size());
        // Act
        assertTrue(requestHeaders.remove("key", "value2"));
        assertFalse(requestHeaders.remove("key", "value2"));
        // Assert
        assertEquals(1, requestHeaders.size());
        assertFalse(requestHeaders.isEmpty());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());

        assertTrue(requestHeaders.remove("key", "value"));
        assertFalse(requestHeaders.remove("key", "value"));
        assertEquals(0, requestHeaders.size());
        assertTrue(requestHeaders.isEmpty());
    }

    @Test
    void PutsAll() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        assertTrue(requestHeaders.isEmpty());
        // Act
        requestHeaders.putAll(
                new RequestHeaders() {
                    {
                        add("key", "value");
                    }
                });
        // Assert
        assertEquals(1, requestHeaders.size());
        assertEquals(1, requestHeaders.get("key").size());
        assertEquals("value", requestHeaders.get("key").iterator().next());
        assertTrue(requestHeaders.containsKey("key"));
        assertFalse(requestHeaders.isEmpty());
    }

    @Test
    void Clears() {
        // Arrange
        final RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.add("key", "value");
        assertEquals(1, requestHeaders.size());
        // Act
        requestHeaders.clear();
        // Assert
        assertEquals(0, requestHeaders.size());
        assertTrue(requestHeaders.isEmpty());
    }
}
