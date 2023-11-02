package com.microsoft.kiota;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.annotation.Nonnull;
/**
 * Compatibility methods for android
 */
public class Compatibility {
	private Compatibility() {
	}
	/**
	 * INTERNAL METHOD, DO NOT USE DIRECTLY
	 * Reads all bytes from the given input stream
	 * @param inputStream the input stream to read from
	 * @return the bytes read from the stream
	 * @throws IOException when the stream cannot be closed or read.
	 */
	@Nonnull
	public static byte[] readAllBytes(@Nonnull final InputStream inputStream) throws IOException {
		// InputStream.readAllBytes() is only available to Android API level 33+
		final int bufLen = 1024;
		byte[] buf = new byte[bufLen];
		int readLen;
		try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
				outputStream.write(buf, 0, readLen);
			return outputStream.toByteArray();
		}
	}
}
