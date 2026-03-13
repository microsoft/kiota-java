package com.microsoft.kiota.serialization;

import com.microsoft.kiota.PeriodAndDuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** ParseNode implementation for URI form encoded payloads */
public class FormParseNode implements ParseNode {
    private final String rawStringValue;
    private final String encoding = StandardCharsets.UTF_8.name();
    private final HashMap<String, String> fields = new HashMap<>();

    /**
     * Initializes a new instance of the {@link FormParseNode} class.
     * @param rawString the raw string value to parse.
     */
    public FormParseNode(@Nonnull final String rawString) {
        Objects.requireNonNull(rawString, "parameter node cannot be null");
        rawStringValue = rawString;
        for (final String kv : rawString.split("&")) {
            final String[] split = kv.split("=");
            final String key = sanitizeKey(split[0]);
            if (split.length == 2) {
                if (fields.containsKey(key))
                    fields.put(key, fields.get(key).concat("," + split[1].trim()));
                else fields.put(key, split[1].trim());
            }
        }
    }

    @SuppressWarnings("removal")
    protected final void finalize() throws Throwable {
        // this is to prevent finalizer attacks, remove when java 9 is the minimum supported version
    }

    private String sanitizeKey(@Nonnull final String key) {
        Objects.requireNonNull(key);
        try {
            return URLDecoder.decode(key, encoding).trim();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding", e);
        }
    }

    @Nullable public ParseNode getChildNode(@Nonnull final String identifier) {
        Objects.requireNonNull(identifier, "identifier parameter is required");
        final String key = sanitizeKey(identifier);
        if (fields.containsKey(key)) {
            final Consumer<Parsable> onBefore = this.onBeforeAssignFieldValues;
            final Consumer<Parsable> onAfter = this.onAfterAssignFieldValues;
            final FormParseNode result = new FormParseNode(fields.get(key));
            result.setOnBeforeAssignFieldValues(onBefore);
            result.setOnAfterAssignFieldValues(onAfter);
            return result;
        } else return null;
    }

    @Nullable public String getStringValue() {
        try {
            final String decoded = URLDecoder.decode(rawStringValue, encoding);
            if (decoded.equalsIgnoreCase("null")) return null;
            return decoded;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @Nullable public Boolean getBooleanValue() {
        return parseBooleanValue(getStringValue());
    }

    @Nullable public Byte getByteValue() {
        return parseByteValue(getStringValue());
    }

    @Nullable public Short getShortValue() {
        return parseShortValue(getStringValue());
    }

    @Nullable public BigDecimal getBigDecimalValue() {
        return parseBigDecimalValue(getStringValue());
    }

    @Nullable public Integer getIntegerValue() {
        return parseIntegerValue(getStringValue());
    }

    @Nullable public Float getFloatValue() {
        return parseFloatValue(getStringValue());
    }

    @Nullable public Double getDoubleValue() {
        return parseDoubleValue(getStringValue());
    }

    @Nullable public Long getLongValue() {
        return parseLongValue(getStringValue());
    }

    @Nullable public UUID getUUIDValue() {
        return parseUUIDValue(getStringValue());
    }

    @Nullable public OffsetDateTime getOffsetDateTimeValue() {
        return parseOffsetDateTimeValue(getStringValue());
    }

    @Nullable public LocalDate getLocalDateValue() {
        return parseLocalDateValue(getStringValue());
    }

    @Nullable public LocalTime getLocalTimeValue() {
        return parseLocalTimeValue(getStringValue());
    }

    @Nullable public PeriodAndDuration getPeriodAndDurationValue() {
        return parsePeriodAndDurationValue(getStringValue());
    }

    @Nullable public <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass) {
        final String[] primitiveStringCollection = getStringValue().split(",");
        final List<T> result = new ArrayList<>(primitiveStringCollection.length);
        for (final String item : primitiveStringCollection) {
            String decodedItem;
            try {
                decodedItem = URLDecoder.decode(item, encoding);
            } catch (UnsupportedEncodingException e) {
                decodedItem = item;
            }
            result.add(convertPrimitiveValue(decodedItem, targetClass));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Nullable private static <T> T convertPrimitiveValue(
            @Nullable final String value, @Nonnull final Class<T> targetClass) {
        if (targetClass == Boolean.class) {
            return (T) parseBooleanValue(value);
        } else if (targetClass == Short.class) {
            return (T) parseShortValue(value);
        } else if (targetClass == Byte.class) {
            return (T) parseByteValue(value);
        } else if (targetClass == BigDecimal.class) {
            return (T) parseBigDecimalValue(value);
        } else if (targetClass == String.class) {
            return (T) (value != null && value.equalsIgnoreCase("null") ? null : value);
        } else if (targetClass == Integer.class) {
            return (T) parseIntegerValue(value);
        } else if (targetClass == Float.class) {
            return (T) parseFloatValue(value);
        } else if (targetClass == Double.class) {
            return (T) parseDoubleValue(value);
        } else if (targetClass == Long.class) {
            return (T) parseLongValue(value);
        } else if (targetClass == UUID.class) {
            return (T) parseUUIDValue(value);
        } else if (targetClass == OffsetDateTime.class) {
            return (T) parseOffsetDateTimeValue(value);
        } else if (targetClass == LocalDate.class) {
            return (T) parseLocalDateValue(value);
        } else if (targetClass == LocalTime.class) {
            return (T) parseLocalTimeValue(value);
        } else if (targetClass == PeriodAndDuration.class) {
            return (T) parsePeriodAndDurationValue(value);
        } else {
            throw new RuntimeException("unknown type to deserialize " + targetClass.getName());
        }
    }

    @Nullable private static Boolean parseBooleanValue(@Nullable final String value) {
        if (value == null) return null;
        switch (value.toLowerCase(Locale.ROOT)) {
            case "true":
            case "1":
                return true;
            case "false":
            case "0":
                return false;
            default:
                return null;
        }
    }

    @Nullable private static Byte parseByteValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Byte.parseByte(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static Short parseShortValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Short.parseShort(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static BigDecimal parseBigDecimalValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static Integer parseIntegerValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static Float parseFloatValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Float.parseFloat(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static Double parseDoubleValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static Long parseLongValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable private static UUID parseUUIDValue(@Nullable final String value) {
        if (value == null) return null;
        return UUID.fromString(value);
    }

    @Nullable private static OffsetDateTime parseOffsetDateTimeValue(@Nullable final String value) {
        if (value == null) return null;
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(value);
                return localDateTime.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex2) {
                throw ex;
            }
        }
    }

    @Nullable private static LocalDate parseLocalDateValue(@Nullable final String value) {
        if (value == null) return null;
        return LocalDate.parse(value);
    }

    @Nullable private static LocalTime parseLocalTimeValue(@Nullable final String value) {
        if (value == null) return null;
        return LocalTime.parse(value);
    }

    @Nullable private static PeriodAndDuration parsePeriodAndDurationValue(@Nullable final String value) {
        if (value == null) return null;
        return PeriodAndDuration.parse(value);
    }

    @Nullable public <T extends Parsable> List<T> getCollectionOfObjectValues(
            @Nonnull final ParsableFactory<T> factory) {
        throw new RuntimeException(
                "deserialization of collections of is not supported with form encoding");
    }

    @Nullable public <T extends Enum<T>> List<T> getCollectionOfEnumValues(
            @Nonnull final ValuedEnumParser<T> enumParser) {
        Objects.requireNonNull(enumParser, "parameter enumParser cannot be null");
        final String stringValue = getStringValue();
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        } else {
            final String[] array = stringValue.split(",");
            final ArrayList<T> result = new ArrayList<>();
            for (final String item : array) {
                result.add(enumParser.forValue(item));
            }
            return result;
        }
    }

    @Nonnull public <T extends Parsable> T getObjectValue(@Nonnull final ParsableFactory<T> factory) {
        Objects.requireNonNull(factory, "parameter factory cannot be null");
        final T item = factory.create(this);
        assignFieldValues(item, item.getFieldDeserializers());
        return item;
    }

    @Nullable public <T extends Enum<T>> T getEnumValue(@Nonnull final ValuedEnumParser<T> enumParser) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        return enumParser.forValue(rawValue);
    }

    @Nullable public <T extends Enum<T>> EnumSet<T> getEnumSetValue(
            @Nonnull final ValuedEnumParser<T> enumParser) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        final List<T> result = new ArrayList<>();
        final String[] rawValues = rawValue.split(",");
        for (final String rawValueItem : rawValues) {
            final T value = enumParser.forValue(rawValueItem);
            if (value != null) {
                result.add(value);
            }
        }
        return EnumSet.copyOf(result);
    }

    private <T extends Parsable> void assignFieldValues(
            final T item, final Map<String, Consumer<ParseNode>> fieldDeserializers) {
        if (!fields.isEmpty()) {
            if (this.onBeforeAssignFieldValues != null) {
                this.onBeforeAssignFieldValues.accept(item);
            }
            Map<String, Object> itemAdditionalData = null;
            if (item instanceof AdditionalDataHolder) {
                itemAdditionalData = ((AdditionalDataHolder) item).getAdditionalData();
            }
            for (final Map.Entry<String, String> fieldEntry : fields.entrySet()) {
                final String fieldKey = fieldEntry.getKey();
                final Consumer<ParseNode> fieldDeserializer = fieldDeserializers.get(fieldKey);
                final String fieldValue = fieldEntry.getValue();
                if (fieldValue == null) continue;
                if (fieldDeserializer != null) {
                    final Consumer<Parsable> onBefore = this.onBeforeAssignFieldValues;
                    final Consumer<Parsable> onAfter = this.onAfterAssignFieldValues;
                    fieldDeserializer.accept(
                            new FormParseNode(fieldValue) {
                                {
                                    this.setOnBeforeAssignFieldValues(onBefore);
                                    this.setOnAfterAssignFieldValues(onAfter);
                                }
                            });
                } else if (itemAdditionalData != null) itemAdditionalData.put(fieldKey, fieldValue);
            }
            if (this.onAfterAssignFieldValues != null) {
                this.onAfterAssignFieldValues.accept(item);
            }
        }
    }

    @Nullable public Consumer<Parsable> getOnBeforeAssignFieldValues() {
        return this.onBeforeAssignFieldValues;
    }

    @Nullable public Consumer<Parsable> getOnAfterAssignFieldValues() {
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

    @Nullable public byte[] getByteArrayValue() {
        final String base64 = this.getStringValue();
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }
}
