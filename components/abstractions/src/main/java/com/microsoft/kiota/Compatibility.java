package com.microsoft.kiota;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Compatibility methods for android
 */
public class Compatibility {
    private Compatibility() {}

    /**
     * INTERNAL METHOD, DO NOT USE DIRECTLY
     * Reads all bytes from the given input stream
     * @param inputStream the input stream to read from
     * @return the bytes read from the stream
     * @throws IOException when the stream cannot be closed or read.
     */
    @Nonnull public static byte[] readAllBytes(@Nonnull final InputStream inputStream) throws IOException {
        // InputStream.readAllBytes() is only available to Android API level 33+
        final int bufLen = 1024;
        byte[] buf = new byte[bufLen];
        int readLen;
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                outputStream.write(buf, 0, readLen);
            return outputStream.toByteArray();
        }
    }

    /** INTERNAL METHOD, DO NOT USE DIRECTLY
     * Checks if the string is null or empty or blank
     * @param str the string to check
     * @return true if the string is null or empty or blank
     */
    public static boolean isBlank(@Nullable final String str) {
        return str == null || str.trim().isEmpty();
    }
}
