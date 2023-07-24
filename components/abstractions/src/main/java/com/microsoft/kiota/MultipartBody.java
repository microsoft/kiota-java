package com.microsoft.kiota;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.serialization.SerializationWriterFactory;

/**
 * Represents a multipart body for a request or a response.
 */
public class MultipartBody implements Parsable {
	@Nonnull
	private final String boundary = UUID.randomUUID().toString().replace("-", "");
	/** @return the boundary string for the multipart body. */
	@Nonnull
	public String getBoundary() {
		return boundary;
	}
	/**
	 * The request adapter to use for the multipart body serialization.
	 */
	@Nullable
	public RequestAdapter requestAdapter;
	/**
	 * Adds or replaces a part in the multipart body.
	 * @param <T> the type of the part to add or replace.
	 * @param name the name of the part to add or replace.
	 * @param contentType the content type of the part to add or replace.
	 * @param value the value of the part to add or replace.
	 */
	public <T> void addOrReplacePart(@Nonnull final String name, @Nonnull final String contentType, @Nonnull final T value) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(contentType);
		Objects.requireNonNull(value);
		if (contentType.isBlank() || contentType.isEmpty())
			throw new IllegalArgumentException("contentType cannot be blank or empty");
		if (name.isBlank() || name.isEmpty())
			throw new IllegalArgumentException("name cannot be blank or empty");

		final String normalizedName = normalizePartName(name);
		originalNames.put(name, normalizedName);
		parts.put(normalizedName, Map.entry(contentType, value));
	}
	private final Map<String, Map.Entry<String, Object>> parts = new HashMap<>();
	private final Map<String, String> originalNames = new HashMap<>();
	private String normalizePartName(@Nonnull final String original)
	{
		return original.toLowerCase(Locale.ROOT);
	}
	/**
	 * Gets the content type of the part with the specified name.
	 * @param partName the name of the part to get.
	 * @return the content type of the part with the specified name.
	 */
	@Nullable
	public Object getPartValue(@Nonnull final String partName)
	{
		Objects.requireNonNull(partName);
		if (partName.isBlank() || partName.isEmpty())
			throw new IllegalArgumentException("partName cannot be blank or empty");
		final String normalizedName = normalizePartName(partName);
		final Object candidate = parts.get(normalizedName);
		if(candidate == null)
			return null;

		return candidate;
	}
	/**
	 * Gets the content type of the part with the specified name.
	 * @param partName the name of the part to get.
	 * @return the content type of the part with the specified name.
	 */
	public boolean removePart(@Nonnull final String partName)
	{
		Objects.requireNonNull(partName);
		if (partName.isBlank() || partName.isEmpty())
			throw new IllegalArgumentException("partName cannot be blank or empty");
		final String normalizedName = normalizePartName(partName);
		final Object candidate = parts.remove(normalizedName);
		if(candidate == null)
			return false;

		originalNames.remove(partName);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getFieldDeserializers'");
	}

	/** {@inheritDoc} */
	@Override
	public void serialize(@Nonnull final SerializationWriter writer) {
		Objects.requireNonNull(writer);
		final RequestAdapter ra = requestAdapter;
		if (ra == null)
			throw new IllegalStateException("requestAdapter cannot be null");
		if (parts.isEmpty())
			throw new IllegalStateException("multipart body cannot be empty");
		final SerializationWriterFactory serializationFactory = ra.getSerializationWriterFactory();
		boolean isFirst = true;
		for (final Map.Entry<String, Map.Entry<String, Object>> partEntry : parts.entrySet()) {
			try {
				if (isFirst)
					isFirst = false;
				else
					writer.writeStringValue("", "");
				writer.writeStringValue("", getBoundary());
				final String partContentType = partEntry.getValue().getKey();
				writer.writeStringValue("Content-Type", partContentType);
				writer.writeStringValue("Content-Disposition", "form-data; name=\"" + originalNames.get(partEntry.getKey()) + "\"");
				writer.writeStringValue("", "");
				final Object objectValue = partEntry.getValue().getValue();
				if (objectValue instanceof Parsable) {
					try (final SerializationWriter partWriter = serializationFactory.getSerializationWriter(partContentType)) {
						partWriter.writeObjectValue("", ((Parsable)objectValue));
						try (final InputStream partContent = partWriter.getSerializedContent()) {
							if (partContent.markSupported())
								partContent.reset();
							final byte[] bytes = partContent.readAllBytes();
							if (bytes != null)
								writer.writeByteArrayValue("", bytes);
						}
					}
				} else if (objectValue instanceof String) {
					writer.writeStringValue("", (String)objectValue);
				} else if (objectValue instanceof InputStream) {
					final InputStream inputStream = (InputStream)objectValue;
					if (inputStream.markSupported())
						inputStream.reset();
					final byte[] bytes = inputStream.readAllBytes();
					if (bytes != null)
						writer.writeByteArrayValue("", bytes);
				} else if (objectValue instanceof byte[]) {
					writer.writeByteArrayValue("", (byte[])objectValue);
				} else {
					throw new IllegalStateException("Unsupported part type" + objectValue.getClass().getName());
				}
			} catch (final IOException ex) {
				throw new RuntimeException(ex);
			}
			writer.writeStringValue("", "");
			writer.writeStringValue("", "--" + boundary + "--");
		}
	}
	
}
