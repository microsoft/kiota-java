package com.microsoft.kiota.serialization;

import com.google.common.collect.Lists;

import java.lang.UnsupportedOperationException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.AdditionalDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** ParseNode implementation for text/plain */
public class TextParseNode implements ParseNode {
    private final String text;
    private final static String NoStructuredDataMessage = "text does not support structured data";
    /**
     * Initializes a new instance of the {@link TextParseNode} class.
     * @param rawText the raw text to parse.
     */
    public TextParseNode(@Nonnull final String rawText) {
        Objects.requireNonNull(rawText, "parameter node cannot be null");
        text = rawText.startsWith("\"") && rawText.endsWith("\"") ? rawText.substring(1, rawText.length() - 2) : rawText;
    }
    @Nullable
    public ParseNode getChildNode(@Nonnull final String identifier) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nullable
    public String getStringValue() {
        return text;
    }
    @Nullable
    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(text);
    }
    @Nullable
    public Byte getByteValue() {
        return Byte.parseByte(text);
    }
    @Nullable
    public Short getShortValue() {
        return Short.parseShort(text);
    }
    @Nullable
    public BigDecimal getBigDecimalValue() {
        return new BigDecimal(text);
    }
    @Nullable
    public Integer getIntegerValue() {
        return Integer.parseInt(text);
    }
    @Nullable
    public Float getFloatValue() {
        return Float.parseFloat(text);
    }
    @Nullable
    public Double getDoubleValue() {
        return Double.parseDouble(text);
    }
    @Nullable
    public Long getLongValue() {
        return Long.parseLong(text);
    }
    @Nullable
    public UUID getUUIDValue() {
        return UUID.fromString(this.getStringValue());
    }
    @Nullable
    public OffsetDateTime getOffsetDateTimeValue() {
        return OffsetDateTime.parse(this.getStringValue());
    }
    @Nullable
    public LocalDate getLocalDateValue() {
        return LocalDate.parse(this.getStringValue());
    }
    @Nullable
    public LocalTime getLocalTimeValue() {
        return LocalTime.parse(this.getStringValue());
    }
    @Nullable
    public Period getPeriodValue() {
        return Period.parse(this.getStringValue());
    }
    @Nullable
    public <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nullable
    public <T extends Parsable> List<T> getCollectionOfObjectValues(@Nonnull final ParsableFactory<T> factory) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nullable
    public <T extends Enum<T>> List<T> getCollectionOfEnumValues(@Nonnull final Class<T> targetEnum) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nonnull
    public <T extends Parsable> T getObjectValue(@Nonnull final ParsableFactory<T> factory) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nullable
    public <T extends Enum<T>> T getEnumValue(@Nonnull final Class<T> targetEnum) {
        final String rawValue = this.getStringValue();
        if(rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        return getEnumValueInt(rawValue, targetEnum);
    }
    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getEnumValueInt(@Nonnull final String rawValue, @Nonnull final Class<T> targetEnum) {
        try {
            return (T)targetEnum.getMethod("forValue", String.class).invoke(null, rawValue);
        } catch (SecurityException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            return null;
        }
    }
    @Nullable
    public <T extends Enum<T>> EnumSet<T> getEnumSetValue(@Nonnull final Class<T> targetEnum) {
        throw new UnsupportedOperationException(NoStructuredDataMessage);
    }
    @Nullable
    public Consumer<Parsable> getOnBeforeAssignFieldValues() {
        return this.onBeforeAssignFieldValues;
    }
    @Nullable
    public Consumer<Parsable> getOnAfterAssignFieldValues() {
        return this.onAfterAssignFieldValues;
    }
    private Consumer<Parsable> onBeforeAssignFieldValues;
    public void setOnBeforeAssignFieldValues(@Nullable final Consumer<Parsable> value) {
        this.onBeforeAssignFieldValues = value;
    }
    private Consumer<Parsable> onAfterAssignFieldValues;
    public void setOnAfterAssignFieldValues(@Nullable final Consumer<Parsable> value) {
        this.onAfterAssignFieldValues = value;
    }
    @Nullable
    public byte[] getByteArrayValue() {
        final String base64 = this.getStringValue();
        if(base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }
}
