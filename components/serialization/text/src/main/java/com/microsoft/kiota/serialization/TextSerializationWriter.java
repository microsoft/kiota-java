package com.microsoft.kiota.serialization;

import com.microsoft.kiota.PeriodAndDuration;

import java.lang.Enum;
import java.lang.UnsupportedOperationException;
import java.math.BigDecimal;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Serialization writer implementation for text/plain */
public class TextSerializationWriter implements SerializationWriter {
    private final static String NO_STRUCTURED_DATA_MESSAGE = "text does not support structured data";
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private final OutputStreamWriter writer;
    private boolean written;
    /**
     * Initializes a new instance of the {@link TextSerializationWriter} class.
     */
    public TextSerializationWriter() {
        try {
            this.writer = new OutputStreamWriter(this.stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeStringValue(@Nullable final String key, @Nullable final String value) {
        if(key != null && !key.isEmpty())
            throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
        if(value != null && !value.isEmpty())
            if(written) {
                throw new UnsupportedOperationException("a value was already written for this serialization writer, text content only supports a single value");
            } else {
                written = true;
                try {
                    writer.write(value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
            writeStringValue(key, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
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
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
    }
    public <T extends Parsable> void writeCollectionOfObjectValues(@Nullable final String key, @Nullable final Iterable<T> values) {
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
    }
    public <T extends Enum<T>> void writeCollectionOfEnumValues(@Nullable final String key, @Nullable final Iterable<T> values) {
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
    }
    public <T extends Parsable> void writeObjectValue(@Nullable final String key, @Nullable final T value, @Nonnull final Parsable ...additionalValuesToMerge) {
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
    }
    public <T extends Enum<T>> void writeEnumSetValue(@Nullable final String key, @Nullable final EnumSet<T> values) {
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
    }
    public <T extends Enum<T>> void writeEnumValue(@Nullable final String key, @Nullable final T value) {
        if(value != null) {
            writeStringValue(key, getStringValueFromValuedEnum(value));
        }
    }
    public void writeNullValue(@Nullable final String key) {
        writeStringValue(null, "null");
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
        throw new UnsupportedOperationException(NO_STRUCTURED_DATA_MESSAGE);
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
    public void writeByteArrayValue(@Nullable final String key, @Nonnull final byte[] value) {
        if(value != null)
            this.writeStringValue(key, Base64.getEncoder().encodeToString(value));
    }
}
