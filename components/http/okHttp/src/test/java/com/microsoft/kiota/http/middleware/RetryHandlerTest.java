package com.microsoft.kiota.http.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.kiota.http.middleware.options.RetryHandlerOption;

import io.opentelemetry.api.trace.Span;

import okhttp3.Request;
import okhttp3.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class RetryHandlerTest {

    private static final double DELAY_MILLISECONDS = 1000.0;
    private static final double DEFAULT_DELAY_MILLISECONDS = -1.0;
    private static final double DELTA = 0.01;

    private RetryHandler retryHandler;

    @BeforeEach
    void setUp() {
        retryHandler = new RetryHandler();
    }

    private static Stream<Arguments> retryAfterHeaderValues() {
        return Stream.of(
                // Single values
                Arguments.of("10", 10.0 * DELAY_MILLISECONDS),
                Arguments.of("0", DEFAULT_DELAY_MILLISECONDS), // Zero should return default
                Arguments.of("-5", DEFAULT_DELAY_MILLISECONDS), // Negative should return default

                // Comma-separated values
                Arguments.of("31,120", 31.0 * DELAY_MILLISECONDS), // First positive value
                Arguments.of("0,45", 45.0 * DELAY_MILLISECONDS), // Skip zero, take next
                Arguments.of("-10,25", 25.0 * DELAY_MILLISECONDS), // Skip negative, take next
                Arguments.of("0,-5,60", 60.0 * DELAY_MILLISECONDS), // Skip multiple invalid

                // Edge cases
                Arguments.of("", DEFAULT_DELAY_MILLISECONDS), // Empty string
                Arguments.of("  ", DEFAULT_DELAY_MILLISECONDS), // Whitespace only
                Arguments.of("abc", DEFAULT_DELAY_MILLISECONDS), // Invalid number
                Arguments.of("10,abc,20", 10.0 * DELAY_MILLISECONDS), // Mixed valid/invalid
                Arguments.of("abc,20", 20.0 * DELAY_MILLISECONDS), // Invalid first, valid second
                Arguments.of("0,0,0", DEFAULT_DELAY_MILLISECONDS), // All zeros
                Arguments.of("-1,-2,-3", DEFAULT_DELAY_MILLISECONDS), // All negative

                // Whitespace handling
                Arguments.of(" 15 ", 15.0 * DELAY_MILLISECONDS), // Whitespace around value
                Arguments.of("10, 20", 10.0 * DELAY_MILLISECONDS), // Whitespace after comma
                Arguments.of(" 0 , 30 ", 30.0 * DELAY_MILLISECONDS) // Multiple whitespaces
                );
    }

    @ParameterizedTest
    @MethodSource("retryAfterHeaderValues")
    void testTryParseTimeHeader(String headerValue, double expectedDelay) {
        double result = retryHandler.tryParseTimeHeader(headerValue);
        assertEquals(
                expectedDelay, result, DELTA, "Failed for header value: '" + headerValue + "'");
    }

    @Test
    void testGetRetryAfterReturnsNegativeWhenHeaderParsingFails() {
        // Create a mock response with a Retry-After header that will fail parsing
        Response mockResponse = mock(Response.class);
        when(mockResponse.header("Retry-After")).thenReturn("invalid");
        when(mockResponse.code()).thenReturn(429);

        // getRetryAfter should return -1 when parsing fails but header exists
        long retryAfter = retryHandler.getRetryAfter(mockResponse, 3, 1);
        assertEquals(-1, retryAfter, "Expected -1 when Retry-After header parsing fails");
    }

    @Test
    void testRetryRequestHandlesNegativeRetryInterval() throws Exception {
        // Create mocks
        Response mockResponse = mock(Response.class);
        Request mockRequest = mock(Request.class);
        RetryHandlerOption mockOption = mock(RetryHandlerOption.class);
        Span mockSpan = mock(Span.class);

        // Setup mock behaviors
        when(mockResponse.code()).thenReturn(429);
        when(mockResponse.header("Retry-After")).thenReturn("invalid");
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.body()).thenReturn(null);
        when(mockOption.maxRetries()).thenReturn(3);
        when(mockOption.delay()).thenReturn(3L);
        when(mockOption.shouldRetry())
                .thenReturn(
                        (delay, executionCount, request, response) ->
                                true); // Always retry for this test

        // Call retryRequest - it should not throw IllegalArgumentException
        // The main goal is to verify no exception is thrown when retry interval is negative
        boolean result =
                retryHandler.retryRequest(mockResponse, 1, mockRequest, mockOption, mockSpan);

        // Verify the method returned true (should retry)
        assertTrue(result, "Expected retryRequest to return true");
    }

    @Test
    void testRetryRequestWithNegativeRetryIntervalAppliesRandomDelay() throws Exception {
        // Create mocks
        Response mockResponse = mock(Response.class);
        Request mockRequest = mock(Request.class);
        RetryHandlerOption mockOption = mock(RetryHandlerOption.class);
        Span mockSpan = mock(Span.class);

        // Setup mock behaviors to force negative retry interval
        when(mockResponse.code()).thenReturn(503);
        when(mockResponse.header("Retry-After")).thenReturn("invalid");
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.body()).thenReturn(null);
        when(mockOption.maxRetries()).thenReturn(5);
        when(mockOption.delay()).thenReturn(1L);
        when(mockOption.shouldRetry())
                .thenReturn((delay, executionCount, request, response) -> true);

        // Test multiple times to verify no exception is thrown
        // This verifies the fix for negative retry intervals
        for (int i = 0; i < 5; i++) {
            boolean result =
                    retryHandler.retryRequest(mockResponse, 1, mockRequest, mockOption, mockSpan);
            // Verify each call succeeds without throwing IllegalArgumentException
            assertTrue(result, "Expected retryRequest to return true on iteration " + i);
        }
    }
}
