package com.microsoft.kiota.serialization;

import com.google.gson.stream.JsonWriter;
import com.microsoft.kiota.PeriodAndDuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Serialization writer implementation for JSON */
public class JsonSerializationWriter implements SerializationWriter {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final JsonWriter writer;

    /** Creates a new instance of a json serialization writer */
    public JsonSerializationWriter() {
        this.writer = new JsonWriter(new OutputStreamWriter(this.stream, StandardCharsets.UTF_8));
    }

    public void writeStringValue(@Nullable final String key, @Nullable final String value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeBooleanValue(@Nullable final String key, @Nullable final Boolean value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeShortValue(@Nullable final String key, @Nullable final Short value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeByteValue(@Nullable final String key, @Nullable final Byte value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeBigDecimalValue(@Nullable final String key, @Nullable final BigDecimal value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeIntegerValue(@Nullable final String key, @Nullable final Integer value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeFloatValue(@Nullable final String key, @Nullable final Float value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeDoubleValue(@Nullable final String key, @Nullable final Double value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeLongValue(@Nullable final String key, @Nullable final Long value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value);
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeUUIDValue(@Nullable final String key, @Nullable final UUID value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value.toString());
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeOffsetDateTimeValue(
            @Nullable final String key, @Nullable final OffsetDateTime value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeLocalDateValue(@Nullable final String key, @Nullable final LocalDate value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writeLocalTimeValue(@Nullable final String key, @Nullable final LocalTime value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value.format(DateTimeFormatter.ISO_LOCAL_TIME));
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public void writePeriodAndDurationValue(
            @Nullable final String key, @Nullable final PeriodAndDuration value) {
        if (value != null)
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.value(value.toString());
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
    }

    public <T> void writeCollectionOfPrimitiveValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        try {
            if (values != null) { // empty array is meaningful
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.beginArray();
                for (final T t : values) {
                    this.writeAnyValue(null, t);
                }
                writer.endArray();
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    public <T extends Parsable> void writeCollectionOfObjectValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        try {
            if (values != null) { // empty array is meaningful
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.beginArray();
                for (final T t : values) {
                    this.writeObjectValue(null, t);
                }
                writer.endArray();
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    public <T extends Enum<T>> void writeCollectionOfEnumValues(
            @Nullable final String key, @Nullable final Iterable<T> values) {
        try {
            if (values != null) { // empty array is meaningful
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.beginArray();
                for (final T t : values) {
                    this.writeEnumValue(null, t);
                }
                writer.endArray();
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    public <T extends Parsable> void writeObjectValue(
            @Nullable final String key,
            @Nullable final T value,
            @Nonnull final Parsable... additionalValuesToMerge) {
        Objects.requireNonNull(additionalValuesToMerge);
        try {
            final List<Parsable> nonNullAdditionalValuesToMerge =
                    Stream.of(additionalValuesToMerge)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            final boolean serializingUntypedNode = value instanceof UntypedNode;
            if (serializingUntypedNode) {
                if (onBeforeObjectSerialization != null && value != null) {
                    onBeforeObjectSerialization.accept(value);
                }
                writeUntypedValue(key, (UntypedNode) value);
                if (onAfterObjectSerialization != null && value != null) {
                    onAfterObjectSerialization.accept(value);
                }
            } else if (value != null || !nonNullAdditionalValuesToMerge.isEmpty()) {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                if (onBeforeObjectSerialization != null && value != null) {
                    onBeforeObjectSerialization.accept(value);
                }
                final boolean serializingComposedType = value instanceof ComposedTypeWrapper;
                if (!serializingComposedType) {
                    writer.beginObject();
                }
                if (value != null) {
                    if (onStartObjectSerialization != null) {
                        onStartObjectSerialization.accept(value, this);
                    }
                    value.serialize(this);
                }
                for (final Parsable additionalValueToMerge : nonNullAdditionalValuesToMerge) {
                    if (onBeforeObjectSerialization != null) {
                        onBeforeObjectSerialization.accept(additionalValueToMerge);
                    }
                    if (onStartObjectSerialization != null) {
                        onStartObjectSerialization.accept(additionalValueToMerge, this);
                    }
                    additionalValueToMerge.serialize(this);
                    if (onAfterObjectSerialization != null) {
                        onAfterObjectSerialization.accept(additionalValueToMerge);
                    }
                }
                if (!serializingComposedType) {
                    writer.endObject();
                }
                if (onAfterObjectSerialization != null && value != null) {
                    onAfterObjectSerialization.accept(value);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    private void writeUntypedValue(String key, UntypedNode value) {
        final Class<?> valueClass = value.getClass();
        if (valueClass.equals(UntypedString.class))
            this.writeStringValue(key, ((UntypedString) value).getValue());
        else if (valueClass.equals(UntypedNull.class)) this.writeNullValue(key);
        else if (valueClass.equals(UntypedDecimal.class))
            this.writeBigDecimalValue(key, ((UntypedDecimal) value).getValue());
        else if (valueClass.equals(UntypedBoolean.class))
            this.writeBooleanValue(key, ((UntypedBoolean) value).getValue());
        else if (valueClass.equals(UntypedFloat.class))
            this.writeFloatValue(key, ((UntypedFloat) value).getValue());
        else if (valueClass.equals(UntypedDouble.class))
            this.writeDoubleValue(key, ((UntypedDouble) value).getValue());
        else if (valueClass.equals(UntypedLong.class))
            this.writeLongValue(key, ((UntypedLong) value).getValue());
        else if (valueClass.equals(UntypedInteger.class))
            this.writeIntegerValue(key, ((UntypedInteger) value).getValue());
        else if (valueClass.equals(UntypedObject.class))
            this.writeUntypedObject(key, (UntypedObject) value);
        else if (valueClass.equals(UntypedArray.class))
            this.writeUntypedArray(key, (UntypedArray) value);
        else throw new RuntimeException("unknown type to serialize " + valueClass.getName());
    }

    private void writeUntypedObject(String key, UntypedObject value) {
        if (value != null) {
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.beginObject();
                for (final Map.Entry<String, UntypedNode> fieldEntry :
                        value.getValue().entrySet()) {
                    final String fieldKey = fieldEntry.getKey();
                    final UntypedNode fieldValue = fieldEntry.getValue();
                    this.writeUntypedValue(fieldKey, fieldValue);
                }
                writer.endObject();
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
        }
    }

    private void writeUntypedArray(String key, UntypedArray value) {
        if (value != null) {
            try {
                if (key != null && !key.isEmpty()) {
                    writer.name(key);
                }
                writer.beginArray();
                for (final UntypedNode entry : value.getValue()) {
                    this.writeUntypedValue(null, entry);
                }
                writer.endArray();
            } catch (IOException ex) {
                throw new RuntimeException("could not serialize value", ex);
            }
        }
    }

    public <T extends Enum<T>> void writeEnumSetValue(
            @Nullable final String key, @Nullable final EnumSet<T> values) {
        if (values != null && !values.isEmpty()) {
            final Optional<String> concatenatedValue =
                    values.stream()
                            .map(v -> this.getStringValueFromValuedEnum(v))
                            .reduce(
                                    (x, y) -> {
                                        return x + "," + y;
                                    });
            if (concatenatedValue.isPresent()) {
                this.writeStringValue(key, concatenatedValue.get());
            }
        }
    }

    public <T extends Enum<T>> void writeEnumValue(
            @Nullable final String key, @Nullable final T value) {
        if (value != null) {
            this.writeStringValue(key, getStringValueFromValuedEnum(value));
        }
    }

    public void writeNullValue(@Nullable final String key) {
        try {
            if (key != null && !key.isEmpty()) {
                writer.name(key);
            }
            writer.nullValue();
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    private <T extends Enum<T>> String getStringValueFromValuedEnum(final T value) {
        if (value instanceof ValuedEnum) {
            final ValuedEnum valued = (ValuedEnum) value;
            return valued.getValue();
        } else return null;
    }

    @Nonnull public InputStream getSerializedContent() {
        try {
            this.writer.flush();
            return new ByteArrayInputStream(this.stream.toByteArray());
            // This copies the whole array in memory could result in memory pressure for large
            // objects, we might want to replace by some kind of piping in the future
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void close() throws IOException {
        this.writer.close();
        this.stream.close();
    }

    public void writeAdditionalData(@Nonnull final Map<String, Object> value) {
        if (value == null) return;
        for (final Map.Entry<String, Object> dataValue : value.entrySet()) {
            this.writeAnyValue(dataValue.getKey(), dataValue.getValue());
        }
    }

    private void writeNonParsableObject(@Nullable final String key, @Nullable final Object value) {
        try {
            if (key != null && !key.isEmpty()) this.writer.name(key);
            if (value == null) this.writer.nullValue();
            else {
                final Class<?> valueClass = value.getClass();
                for (final Field oProp : valueClass.getFields())
                    this.writeAnyValue(oProp.getName(), oProp.get(value));
            }
        } catch (IOException | IllegalAccessException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }

    private void writeAnyValue(@Nullable final String key, @Nullable final Object value) {
        if (value == null) {
            this.writeNullValue(key);
        } else {
            final Class<?> valueClass = value.getClass();
            if (valueClass.equals(String.class)) this.writeStringValue(key, (String) value);
            else if (valueClass.equals(Boolean.class)) this.writeBooleanValue(key, (Boolean) value);
            else if (valueClass.equals(Byte.class)) this.writeByteValue(key, (Byte) value);
            else if (valueClass.equals(Short.class)) this.writeShortValue(key, (Short) value);
            else if (valueClass.equals(BigDecimal.class))
                this.writeBigDecimalValue(key, (BigDecimal) value);
            else if (valueClass.equals(Float.class)) this.writeFloatValue(key, (Float) value);
            else if (valueClass.equals(Long.class)) this.writeLongValue(key, (Long) value);
            else if (valueClass.equals(Integer.class)) this.writeIntegerValue(key, (Integer) value);
            else if (valueClass.equals(UUID.class)) this.writeUUIDValue(key, (UUID) value);
            else if (valueClass.equals(OffsetDateTime.class))
                this.writeOffsetDateTimeValue(key, (OffsetDateTime) value);
            else if (valueClass.equals(LocalDate.class))
                this.writeLocalDateValue(key, (LocalDate) value);
            else if (valueClass.equals(LocalTime.class))
                this.writeLocalTimeValue(key, (LocalTime) value);
            else if (value instanceof UntypedNode) this.writeUntypedValue(key, (UntypedNode) value);
            else if (valueClass.equals(PeriodAndDuration.class))
                this.writePeriodAndDurationValue(key, (PeriodAndDuration) value);
            else if (value instanceof Iterable<?>)
                this.writeCollectionOfPrimitiveValues(key, (Iterable<?>) value);
            else if (!valueClass.isPrimitive()) this.writeNonParsableObject(key, value);
            else throw new RuntimeException("unknown type to serialize " + valueClass.getName());
        }
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

    public void writeByteArrayValue(@Nullable final String key, @Nullable final byte[] value) {
        if (value != null) this.writeStringValue(key, Base64.getEncoder().encodeToString(value));
    }
}
