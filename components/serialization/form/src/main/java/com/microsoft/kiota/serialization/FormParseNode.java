package com.microsoft.kiota.serialization;

import com.microsoft.kiota.PeriodAndDuration;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
        switch (getStringValue()
                .toLowerCase(
                        Locale.ROOT)) { // boolean parse returns false for any value that is not
                // true
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

    @Nullable public Byte getByteValue() {
        try {
            return Byte.parseByte(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public Short getShortValue() {
        try {
            return Short.parseShort(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public BigDecimal getBigDecimalValue() {
        try {
            return new BigDecimal(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public Integer getIntegerValue() {
        try {
            return Integer.parseInt(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public Float getFloatValue() {
        try {
            return Float.parseFloat(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public Double getDoubleValue() {
        try {
            return Double.parseDouble(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public Long getLongValue() {
        try {
            return Long.parseLong(getStringValue());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }

    @Nullable public UUID getUUIDValue() {
        final String stringValue = getStringValue();
        if (stringValue == null) return null;
        return UUID.fromString(stringValue);
    }

    @Nullable public OffsetDateTime getOffsetDateTimeValue() {
        final String stringValue = getStringValue();
        if (stringValue == null) return null;
        return OffsetDateTime.parse(stringValue);
    }

    @Nullable public LocalDate getLocalDateValue() {
        final String stringValue = getStringValue();
        if (stringValue == null) return null;
        return LocalDate.parse(stringValue);
    }

    @Nullable public LocalTime getLocalTimeValue() {
        final String stringValue = getStringValue();
        if (stringValue == null) return null;
        return LocalTime.parse(stringValue);
    }

    @Nullable public PeriodAndDuration getPeriodAndDurationValue() {
        final String stringValue = getStringValue();
        if (stringValue == null) return null;
        return PeriodAndDuration.parse(stringValue);
    }

    @Nullable public <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass) {
        final List<String> primitiveStringCollection = Arrays.asList(getStringValue().split(","));
        final Iterator<String> sourceIterator = primitiveStringCollection.iterator();
        final FormParseNode _this = this;
        final List<T> result = new ArrayList<>();
        final Iterable<T> iterable =
                new Iterable<T>() {
                    @Override
                    public Iterator<T> iterator() {
                        return new Iterator<T>() {
                            @Override
                            public boolean hasNext() {
                                return sourceIterator.hasNext();
                            }

                            @Override
                            @SuppressWarnings("unchecked")
                            public T next() {
                                final String item = sourceIterator.next();
                                final Consumer<Parsable> onBefore =
                                        _this.getOnBeforeAssignFieldValues();
                                final Consumer<Parsable> onAfter =
                                        _this.getOnAfterAssignFieldValues();
                                final FormParseNode itemNode =
                                        new FormParseNode(item) {
                                            {
                                                this.setOnBeforeAssignFieldValues(onBefore);
                                                this.setOnAfterAssignFieldValues(onAfter);
                                            }
                                        };
                                if (targetClass == Boolean.class) {
                                    return (T) itemNode.getBooleanValue();
                                } else if (targetClass == Short.class) {
                                    return (T) itemNode.getShortValue();
                                } else if (targetClass == Byte.class) {
                                    return (T) itemNode.getByteValue();
                                } else if (targetClass == BigDecimal.class) {
                                    return (T) itemNode.getBigDecimalValue();
                                } else if (targetClass == String.class) {
                                    return (T) itemNode.getStringValue();
                                } else if (targetClass == Integer.class) {
                                    return (T) itemNode.getIntegerValue();
                                } else if (targetClass == Float.class) {
                                    return (T) itemNode.getFloatValue();
                                } else if (targetClass == Long.class) {
                                    return (T) itemNode.getLongValue();
                                } else if (targetClass == UUID.class) {
                                    return (T) itemNode.getUUIDValue();
                                } else if (targetClass == OffsetDateTime.class) {
                                    return (T) itemNode.getOffsetDateTimeValue();
                                } else if (targetClass == LocalDate.class) {
                                    return (T) itemNode.getLocalDateValue();
                                } else if (targetClass == LocalTime.class) {
                                    return (T) itemNode.getLocalTimeValue();
                                } else if (targetClass == PeriodAndDuration.class) {
                                    return (T) itemNode.getPeriodAndDurationValue();
                                } else {
                                    throw new RuntimeException(
                                            "unknown type to deserialize " + targetClass.getName());
                                }
                            }
                        };
                    }
                };

        for (T elem : iterable) {
            result.add(elem);
        }
        return result;
    }

    @Nullable public <T extends Parsable> List<T> getCollectionOfObjectValues(
            @Nonnull final ParsableFactory<T> factory) {
        throw new RuntimeException(
                "deserialization of collections of is not supported with form encoding");
    }

    @Nullable public <T extends Enum<T>> List<T> getCollectionOfEnumValues(
            @Nonnull final Function<String, T> fromValue) {
        Objects.requireNonNull(fromValue, "parameter fromValue cannot be null");
        final String stringValue = getStringValue();
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        } else {
            final String[] array = stringValue.split(",");
            final ArrayList<T> result = new ArrayList<>();
            for (final String item : array) {
                result.add(fromValue.apply(item));
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

    @Nullable public <T extends Enum<T>> T getEnumValue(@Nonnull final Function<String, T> forValue) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        return forValue.apply(rawValue);
    }

    @Nullable public <T extends Enum<T>> EnumSet<T> getEnumSetValue(
            @Nonnull final Function<String, T> forValue) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        final List<T> result = new ArrayList<>();
        final String[] rawValues = rawValue.split(",");
        for (final String rawValueItem : rawValues) {
            final T value = forValue.apply(rawValueItem);
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
