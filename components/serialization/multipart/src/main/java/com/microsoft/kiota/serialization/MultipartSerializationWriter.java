package com.microsoft.kiota.serialization;

import com.microsoft.kiota.MultipartBody;
import com.microsoft.kiota.PeriodAndDuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Serialization writer implementation for Multipart encoded payloads */
public class MultipartSerializationWriter implements SerializationWriter {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final OutputStreamWriter writer;

    /** Instantiates a new MultipartSerializationWriter. */
    public MultipartSerializationWriter() {
        try {
            this.writer = new OutputStreamWriter(this.stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("could not create writer", e);
        }
    }

    public void writeStringValue(@Nullable final String key, @Nullable final String value) {
        try {
            if (key != null && !key.isEmpty()) writer.write(key);
            if (value != null && !value.isEmpty()) {
                if (key != null && !key.isEmpty()) writer.write(": ");
                writer.write(value);
            }
            writer.write("\r\n");
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    public void writeBooleanValue(@Nullable final String key, @Nullable final Boolean value) {
        throw new UnsupportedOperationException();
    }

    public void writeShortValue(@Nullable final String key, @Nullable final Short value) {
        throw new UnsupportedOperationException();
    }

    public void writeByteValue(@Nullable final String key, @Nullable final Byte value) {
        throw new UnsupportedOperationException();
    }

    public void writeBigDecimalValue(@Nullable final String key, @Nullable final BigDecimal value) {
        throw new UnsupportedOperationException();
    }

    public void writeIntegerValue(@Nullable final String key, @Nullable final Integer value) {
        throw new UnsupportedOperationException();
    }

    public void writeFloatValue(@Nullable final String key, @Nullable final Float value) {
        throw new UnsupportedOperationException();
    }

    public void writeDoubleValue(@Nullable final String key, @Nullable final Double value) {
        throw new UnsupportedOperationException();
    }

    public void writeLongValue(@Nullable final String key, @Nullable final Long value) {
        throw new UnsupportedOperationException();
    }

    public void writeUUIDValue(@Nullable final String key, @Nullable final UUID value) {
        throw new UnsupportedOperationException();
    }

    public void writeOffsetDateTimeValue(
            @Nullable final String key, @Nullable final OffsetDateTime value) {
        throw new UnsupportedOperationException();
    }

    public void writeLocalDateValue(@Nullable final String key, @Nullable final LocalDate value) {
        throw new UnsupportedOperationException();
    }

    public void writeLocalTimeValue(@Nullable final String key, @Nullable final LocalTime value) {
        throw new UnsupportedOperationException();
    }

    public void writePeriodAndDurationValue(
            @Nullable final String key, @Nullable final PeriodAndDuration value) {
        throw new UnsupportedOperationException();
    }

    public <T> void writeCollectionOfPrimitiveValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        throw new UnsupportedOperationException();
    }

    public <T extends Parsable> void writeCollectionOfObjectValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        throw new UnsupportedOperationException();
    }

    public <T extends Enum<T>> void writeCollectionOfEnumValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        throw new UnsupportedOperationException();
    }

    public <T extends Parsable> void writeObjectValue(
            @Nullable final String key,
            @Nullable final T value,
            @Nonnull final Parsable... additionalValuesToMerge) {
        Objects.requireNonNull(additionalValuesToMerge);
        if (value != null) {
            if (onBeforeObjectSerialization != null) {
                onBeforeObjectSerialization.accept(value);
            }
            if (value instanceof MultipartBody) {
                if (onStartObjectSerialization != null) {
                    onStartObjectSerialization.accept(value, this);
                }
                value.serialize(this);
            } else {
                throw new RuntimeException(
                        "expected MultipartBody instance but got " + value.getClass().getName());
            }
            if (onAfterObjectSerialization != null) {
                onAfterObjectSerialization.accept(value);
            }
        }
    }

    public <T extends Enum<T>> void writeEnumSetValue(
            @Nullable final String key, @Nullable final EnumSet<T> values) {
        throw new UnsupportedOperationException();
    }

    public <T extends Enum<T>> void writeEnumValue(
            @Nullable final String key, @Nullable final T value) {
        throw new UnsupportedOperationException();
    }

    public void writeNullValue(@Nullable final String key) {
        throw new UnsupportedOperationException();
    }

    @Nonnull public InputStream getSerializedContent() {
        return new ByteArrayInputStream(this.stream.toByteArray());
    }

    public void close() throws IOException {
        this.writer.close();
        this.stream.close();
    }

    public void writeAdditionalData(@Nonnull final Map<String, Object> value) {
        throw new UnsupportedOperationException();
    }

    @Nullable public Consumer<Parsable> getOnBeforeObjectSerialization() {
        return this.onBeforeObjectSerialization;
    }

    @Nullable public Consumer<Parsable> getOnAfterObjectSerialization() {
        return this.onAfterObjectSerialization;
    }

    @Nullable public BiConsumer<Parsable, SerializationWriter> getOnStartObjectSerialization() {
        return this.onStartObjectSerialization;
    }

    private Consumer<Parsable> onBeforeObjectSerialization;

    public void setOnBeforeObjectSerialization(@Nullable final Consumer<Parsable> value) {
        this.onBeforeObjectSerialization = value;
    }

    private Consumer<Parsable> onAfterObjectSerialization;

    public void setOnAfterObjectSerialization(@Nullable final Consumer<Parsable> value) {
        this.onAfterObjectSerialization = value;
    }

    private BiConsumer<Parsable, SerializationWriter> onStartObjectSerialization;

    public void setOnStartObjectSerialization(
            @Nullable final BiConsumer<Parsable, SerializationWriter> value) {
        this.onStartObjectSerialization = value;
    }

    public void writeByteArrayValue(
            @Nullable final String key, @Nullable @Nonnull final byte[] value) {
        if (value != null)
            try {
                this.stream.write(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }
}
