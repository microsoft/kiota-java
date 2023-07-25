package com.microsoft.kiota.serialization;

import com.microsoft.kiota.PeriodAndDuration;

import java.lang.Enum;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Serialization writer implementation for Multipart encoded payloads */
public class MultipartSerializationWriter implements SerializationWriter {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final OutputStreamWriter writer;
    private final String encoding  = StandardCharsets.UTF_8.name();
    private boolean written;
    private int depth = 0;
    /** Instantiates a new MultipartSerializationWriter. */
    public MultipartSerializationWriter() {
        try {
            this.writer = new OutputStreamWriter(this.stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("could not create writer", e);
        }
    }
    public void writeStringValue(@Nullable final String key, @Nullable final String value) {
        if(value == null || key == null || key.isEmpty())
            return;
        try {
            if(written)
                writer.write("&");
            else
                written = true;
            writer.write(URLEncoder.encode(key, encoding) + "=" + URLEncoder.encode(value, encoding));
        } catch (IOException ex) {
            throw new RuntimeException("could not serialize value", ex);
        }
    }
    public void writeBooleanValue(@Nullable final String key, @Nullable final Boolean value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeShortValue(@Nullable final String key, @Nullable final Short value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeByteValue(@Nullable final String key, @Nullable final Byte value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeBigDecimalValue(@Nullable final String key, @Nullable final BigDecimal value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeIntegerValue(@Nullable final String key, @Nullable final Integer value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeFloatValue(@Nullable final String key, @Nullable final Float value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeDoubleValue(@Nullable final String key, @Nullable final Double value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeLongValue(@Nullable final String key, @Nullable final Long value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeUUIDValue(@Nullable final String key, @Nullable final UUID value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public void writeOffsetDateTimeValue(@Nullable final String key, @Nullable final OffsetDateTime value) {
        if(value != null)
            writeStringValue(key, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }
    public void writeLocalDateValue(@Nullable final String key, @Nullable final LocalDate value) {
        if(value != null)
            writeStringValue(key, value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    public void writeLocalTimeValue(@Nullable final String key, @Nullable final LocalTime value) {
        if(value != null)
            writeStringValue(key, value.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }
    public void writePeriodAndDurationValue(@Nullable final String key, @Nullable final PeriodAndDuration value) {
        if(value != null)
            writeStringValue(key, value.toString());
    }
    public <T> void writeCollectionOfPrimitiveValues(@Nullable final String key, @Nullable final Iterable<T> values) {
        if(values != null) {
            for (final T t : values) {
                this.writeAnyValue(key, t);
            }
        }
    }
    public <T extends Parsable> void writeCollectionOfObjectValues(@Nullable final String key, @Nullable final Iterable<T> values) {
        throw new RuntimeException("collections serialization is not supported with form encoding");
    }
    public <T extends Enum<T>> void writeCollectionOfEnumValues(@Nullable final String key, @Nullable final Iterable<T> values) {
        if(values != null) { //empty array is meaningful
            final StringBuffer buffer = new StringBuffer();
            int writtenValuesCount = -1;
            for (final T t : values) {
                if(++writtenValuesCount > 0)
                    buffer.append(",");
                buffer.append(getStringValueFromValuedEnum(t));
            }
            writeStringValue(key, buffer.toString());
        }
    }
    public <T extends Parsable> void writeObjectValue(@Nullable final String key, @Nullable final T value, @Nonnull final Parsable ...additionalValuesToMerge) {
        Objects.requireNonNull(additionalValuesToMerge);
        if (depth > 0)
            throw new RuntimeException("serialization of complex properties is not supported with form encoding");
        depth++;
        final List<Parsable> nonNullAdditionalValuesToMerge = Stream.of(additionalValuesToMerge).filter(Objects::nonNull).collect(Collectors.toList());
        if(value != null || nonNullAdditionalValuesToMerge.size() > 0) {
            if(onBeforeObjectSerialization != null && value != null) {
                onBeforeObjectSerialization.accept(value);
            }
            if(value != null) {
                if(onStartObjectSerialization != null) {
                    onStartObjectSerialization.accept(value, this);
                }
                value.serialize(this);
            }
            for(final Parsable additionalValueToMerge : nonNullAdditionalValuesToMerge) {
                if(onBeforeObjectSerialization != null) {
                    onBeforeObjectSerialization.accept(additionalValueToMerge);
                }
                if(onStartObjectSerialization != null) {
                    onStartObjectSerialization.accept(additionalValueToMerge, this);
                }
                additionalValueToMerge.serialize(this);
                if(onAfterObjectSerialization != null) {
                    onAfterObjectSerialization.accept(additionalValueToMerge);
                }
            }
            if(onAfterObjectSerialization != null && value != null) {
                onAfterObjectSerialization.accept(value);
            }
        }
    }
    public <T extends Enum<T>> void writeEnumSetValue(@Nullable final String key, @Nullable final EnumSet<T> values) {
        if(values != null && !values.isEmpty()) {
            final Optional<String> concatenatedValue = values.stream().map(v -> this.getStringValueFromValuedEnum(v)).reduce((x, y) -> { return x + "," + y; });
            if(concatenatedValue.isPresent()) {
                this.writeStringValue(key, concatenatedValue.get());
            }
        }
    }
    public <T extends Enum<T>> void writeEnumValue(@Nullable final String key, @Nullable final T value) {
        if(value != null) {
            this.writeStringValue(key, getStringValueFromValuedEnum(value));
        }
    }
    public void writeNullValue(@Nullable final String key) {
        writeStringValue(key, "null");
    }
    private <T extends Enum<T>> String getStringValueFromValuedEnum(final T value) {
        if(value instanceof ValuedEnum) {
            final ValuedEnum valued = (ValuedEnum)value;
            return valued.getValue();
        } else return null;
    }
    @Nonnull
    public InputStream getSerializedContent() {
        try {
            this.writer.flush();
            return new ByteArrayInputStream(this.stream.toByteArray());
            //This copies the whole array in memory could result in memory pressure for large objects, we might want to replace by some kind of piping in the future
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void close() throws IOException {
        this.writer.close();
        this.stream.close();
    }
    public void writeAdditionalData(@Nonnull final Map<String, Object> value) {
        if(value == null) return;
        for(final Map.Entry<String, Object> dataValue : value.entrySet()) {
            this.writeAnyValue(dataValue.getKey(), dataValue.getValue());
        }
    }
    private void writeAnyValue(@Nullable final String key, @Nullable final Object value) {
        if(value == null) {
            this.writeNullValue(key);
        } else {
            final Class<?> valueClass = value.getClass();
            if(valueClass.equals(String.class))
                this.writeStringValue(key, (String)value);
            else if(valueClass.equals(Boolean.class))
                this.writeBooleanValue(key, (Boolean)value);
            else if(valueClass.equals(Byte.class))
                this.writeByteValue(key, (Byte)value);
            else if(valueClass.equals(Short.class))
                this.writeShortValue(key, (Short)value);
            else if(valueClass.equals(BigDecimal.class))
                this.writeBigDecimalValue(key, (BigDecimal)value);
            else if(valueClass.equals(Float.class))
                this.writeFloatValue(key, (Float)value);
            else if(valueClass.equals(Long.class))
                this.writeLongValue(key, (Long)value);
            else if(valueClass.equals(Integer.class))
                this.writeIntegerValue(key, (Integer)value);
            else if(valueClass.equals(UUID.class))
                this.writeUUIDValue(key, (UUID)value);
            else if(valueClass.equals(OffsetDateTime.class))
                this.writeOffsetDateTimeValue(key, (OffsetDateTime)value);
            else if(valueClass.equals(LocalDate.class))
                this.writeLocalDateValue(key, (LocalDate)value);
            else if(valueClass.equals(LocalTime.class))
                this.writeLocalTimeValue(key, (LocalTime)value);
            else if(valueClass.equals(PeriodAndDuration.class))
                this.writePeriodAndDurationValue(key, (PeriodAndDuration)value);
            else if(value instanceof Iterable<?>)
                this.writeCollectionOfPrimitiveValues(key, (Iterable<?>)value);
            else
                throw new RuntimeException("unknown type to serialize " + valueClass.getName());
        }
    }
    @Nullable
    public Consumer<Parsable> getOnBeforeObjectSerialization() {
        return this.onBeforeObjectSerialization;
    }
    @Nullable
    public Consumer<Parsable> getOnAfterObjectSerialization() {
        return this.onAfterObjectSerialization;
    }
    @Nullable
    public BiConsumer<Parsable, SerializationWriter> getOnStartObjectSerialization() {
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
    public void setOnStartObjectSerialization(@Nullable final BiConsumer<Parsable, SerializationWriter> value) {
        this.onStartObjectSerialization = value;
    }
    public void writeByteArrayValue(@Nullable final String key, @Nullable @Nonnull final byte[] value) {
        if(value != null)
            this.writeStringValue(key, Base64.getEncoder().encodeToString(value));
    }
}
