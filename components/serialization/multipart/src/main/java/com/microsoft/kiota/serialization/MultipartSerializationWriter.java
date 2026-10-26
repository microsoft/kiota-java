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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Serialization writer implementation for Multipart encoded payloads */
public class MultipartSerializationWriter implements SerializationWriter {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final OutputStreamWriter writer =
            new OutputStreamWriter(stream, StandardCharsets.UTF_8);

    private enum FramingState {
        EXPECTING_FIRST_BOUNDARY,
        IN_HEADERS,
        EXPECTING_PAYLOAD,
        EXPECTING_DELIMITER_AFTER_PAYLOAD,
        EXPECTING_BOUNDARY_OR_CLOSING,
        FINISHED
    }

    private String currentTargetBoundary;
    private String currentReplacementBoundary;
    private boolean isLastBody;
    private FramingState framingState;

    public void writeStringValue(@Nullable final String key, @Nullable final String value) {
        try {
            if (key != null && !key.isEmpty() && value == null) {
                return;
            }
            String valueToWrite = value;
            if (currentTargetBoundary != null
                    && currentReplacementBoundary != null
                    && framingState != null) {
                switch (framingState) {
                    case EXPECTING_FIRST_BOUNDARY:
                        if ((key == null || key.isEmpty())
                                && value != null
                                && value.equals("--" + currentTargetBoundary)) {
                            valueToWrite = "--" + currentReplacementBoundary;
                            framingState = FramingState.IN_HEADERS;
                        }
                        break;
                    case IN_HEADERS:
                        if ((key == null || key.isEmpty()) && (value == null || value.isEmpty())) {
                            framingState = FramingState.EXPECTING_PAYLOAD;
                        }
                        break;
                    case EXPECTING_PAYLOAD:
                        framingState = FramingState.EXPECTING_DELIMITER_AFTER_PAYLOAD;
                        break;
                    case EXPECTING_DELIMITER_AFTER_PAYLOAD:
                        if ((key == null || key.isEmpty()) && (value == null || value.isEmpty())) {
                            framingState = FramingState.EXPECTING_BOUNDARY_OR_CLOSING;
                        }
                        break;
                    case EXPECTING_BOUNDARY_OR_CLOSING:
                        if ((key == null || key.isEmpty()) && value != null) {
                            if (value.equals("--" + currentTargetBoundary)) {
                                valueToWrite = "--" + currentReplacementBoundary;
                                framingState = FramingState.IN_HEADERS;
                            } else if (value.equals("--" + currentTargetBoundary + "--")) {
                                if (isLastBody) {
                                    valueToWrite = "--" + currentReplacementBoundary + "--";
                                    framingState = FramingState.FINISHED;
                                } else {
                                    framingState = FramingState.FINISHED;
                                    return;
                                }
                            }
                        }
                        break;
                    case FINISHED:
                        break;
                }
            }
            if (key != null && !key.isEmpty()) writer.write(key);
            if (valueToWrite != null && !valueToWrite.isEmpty()) {
                if (key != null && !key.isEmpty()) writer.write(": ");
                writer.write(valueToWrite);
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
        final List<MultipartBody> bodies = new ArrayList<>();
        if (value != null) {
            if (value instanceof MultipartBody) {
                bodies.add((MultipartBody) value);
            } else {
                throw new RuntimeException(
                        "expected MultipartBody instance but got " + value.getClass().getName());
            }
        }
        for (final Parsable additionalValueToMerge : additionalValuesToMerge) {
            if (additionalValueToMerge != null) {
                if (additionalValueToMerge instanceof MultipartBody) {
                    bodies.add((MultipartBody) additionalValueToMerge);
                } else {
                    throw new RuntimeException(
                            "expected MultipartBody instance but got "
                                    + additionalValueToMerge.getClass().getName());
                }
            }
        }

        if (bodies.isEmpty()) {
            return;
        }

        final String sharedBoundary = bodies.get(0).getBoundary();
        for (int i = 0; i < bodies.size(); i++) {
            final MultipartBody body = bodies.get(i);
            if (onBeforeObjectSerialization != null) {
                onBeforeObjectSerialization.accept(body);
            }
            if (onStartObjectSerialization != null) {
                onStartObjectSerialization.accept(body, this);
            }

            currentTargetBoundary = body.getBoundary();
            currentReplacementBoundary = sharedBoundary;
            isLastBody = (i == bodies.size() - 1);
            framingState = FramingState.EXPECTING_FIRST_BOUNDARY;
            try {
                body.serialize(this);
            } finally {
                currentTargetBoundary = null;
                currentReplacementBoundary = null;
                isLastBody = false;
                framingState = null;
            }

            if (onAfterObjectSerialization != null) {
                onAfterObjectSerialization.accept(body);
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
        if (framingState == FramingState.EXPECTING_PAYLOAD) {
            framingState = FramingState.EXPECTING_DELIMITER_AFTER_PAYLOAD;
        }
        if (value != null)
            try {
                this.stream.write(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }
}
