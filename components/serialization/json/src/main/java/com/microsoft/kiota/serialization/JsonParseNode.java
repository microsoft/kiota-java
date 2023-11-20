package com.microsoft.kiota.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.kiota.PeriodAndDuration;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** ParseNode implementation for JSON */
public class JsonParseNode implements ParseNode {
    private final JsonElement currentNode;

    /**
     * Creates a new instance of the JsonParseNode class.
     * @param node the node to wrap.
     */
    public JsonParseNode(@Nonnull final JsonElement node) {
        currentNode = Objects.requireNonNull(node, "parameter node cannot be null");
    }

    /** {@inheritDoc} */
    @Nullable public ParseNode getChildNode(@Nonnull final String identifier) {
        Objects.requireNonNull(identifier, "identifier parameter is required");
        if (currentNode.isJsonObject()) {
            final JsonObject object = currentNode.getAsJsonObject();
            final Consumer<Parsable> onBefore = this.onBeforeAssignFieldValues;
            final Consumer<Parsable> onAfter = this.onAfterAssignFieldValues;
            return new JsonParseNode(object.get(identifier)) {
                {
                    this.setOnBeforeAssignFieldValues(onBefore);
                    this.setOnAfterAssignFieldValues(onAfter);
                }
            };
        } else return null;
    }

    @Nullable public String getStringValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsString() : null;
    }

    @Nullable public Boolean getBooleanValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsBoolean() : null;
    }

    @Nullable public Byte getByteValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsByte() : null;
    }

    @Nullable public Short getShortValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsShort() : null;
    }

    @Nullable public BigDecimal getBigDecimalValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsBigDecimal() : null;
    }

    @Nullable public Integer getIntegerValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsInt() : null;
    }

    @Nullable public Float getFloatValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsFloat() : null;
    }

    @Nullable public Double getDoubleValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsDouble() : null;
    }

    @Nullable public Long getLongValue() {
        return currentNode.isJsonPrimitive() ? currentNode.getAsLong() : null;
    }

    @Nullable public UUID getUUIDValue() {
        final String stringValue = currentNode.getAsString();
        if (stringValue == null) return null;
        return UUID.fromString(stringValue);
    }

    @Nullable public OffsetDateTime getOffsetDateTimeValue() {
        final String stringValue = currentNode.getAsString();
        if (stringValue == null) return null;
        return OffsetDateTime.parse(stringValue);
    }

    @Nullable public LocalDate getLocalDateValue() {
        final String stringValue = currentNode.getAsString();
        if (stringValue == null) return null;
        return LocalDate.parse(stringValue);
    }

    @Nullable public LocalTime getLocalTimeValue() {
        final String stringValue = currentNode.getAsString();
        if (stringValue == null) return null;
        return LocalTime.parse(stringValue);
    }

    @Nullable public PeriodAndDuration getPeriodAndDurationValue() {
        final String stringValue = currentNode.getAsString();
        if (stringValue == null) return null;
        return PeriodAndDuration.parse(stringValue);
    }

    @Nullable private <T> T getPrimitiveValue(
            @Nonnull final Class<T> targetClass, @Nonnull final JsonParseNode itemNode) {
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
            throw new RuntimeException("unknown type to deserialize " + targetClass.getName());
        }
    }

    @Nullable public <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass) {
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            final JsonArray array = currentNode.getAsJsonArray();
            final Iterator<JsonElement> sourceIterator = array.iterator();
            final JsonParseNode _this = this;
            final List<T> result = new ArrayList<>();
            while (sourceIterator.hasNext()) {
                JsonElement item = sourceIterator.next();
                JsonParseNode itemNode = new JsonParseNode(item);
                itemNode.setOnBeforeAssignFieldValues(this.getOnBeforeAssignFieldValues());
                itemNode.setOnAfterAssignFieldValues(this.getOnAfterAssignFieldValues());
                result.add(getPrimitiveValue(targetClass, itemNode));
            }
            return result;
        } else throw new RuntimeException("invalid state expected to have an array node");
    }

    @Nullable public <T extends Parsable> List<T> getCollectionOfObjectValues(
            @Nonnull final ParsableFactory<T> factory) {
        Objects.requireNonNull(factory, "parameter factory cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            final JsonArray array = currentNode.getAsJsonArray();
            final Iterator<JsonElement> sourceIterator = array.iterator();
            final JsonParseNode _this = this;
            final List<T> result = new ArrayList<>();
            while (sourceIterator.hasNext()) {
                JsonElement item = sourceIterator.next();
                JsonParseNode itemNode = new JsonParseNode(item);
                itemNode.setOnBeforeAssignFieldValues(this.getOnBeforeAssignFieldValues());
                itemNode.setOnAfterAssignFieldValues(this.getOnAfterAssignFieldValues());
                result.add(itemNode.getObjectValue(factory));
            }
            return result;
        } else return null;
    }

    @Nullable public <T extends Enum<T>> List<T> getCollectionOfEnumValues(
            @Nonnull final Class<T> targetEnum) {
        Objects.requireNonNull(targetEnum, "parameter targetEnum cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            final JsonArray array = currentNode.getAsJsonArray();
            final Iterator<JsonElement> sourceIterator = array.iterator();
            final List<T> result = new ArrayList<>();
            while (sourceIterator.hasNext()) {
                final JsonElement item = sourceIterator.next();
                final JsonParseNode itemNode = new JsonParseNode(item);
                itemNode.setOnBeforeAssignFieldValues(this.getOnBeforeAssignFieldValues());
                itemNode.setOnAfterAssignFieldValues(this.getOnAfterAssignFieldValues());
                result.add(itemNode.getEnumValue(targetEnum));
            }
            return result;
        } else throw new RuntimeException("invalid state expected to have an array node");
    }

    @Nonnull public <T extends Parsable> T getObjectValue(@Nonnull final ParsableFactory<T> factory) {
        Objects.requireNonNull(factory, "parameter factory cannot be null");
        final T item = factory.create(this);
        assignFieldValues(item, item.getFieldDeserializers());
        return item;
    }

    @Nullable public <T extends Enum<T>> T getEnumValue(@Nonnull final Class<T> targetEnum) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        return getEnumValueInt(rawValue, targetEnum);
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getEnumValueInt(
            @Nonnull final String rawValue, @Nonnull final Class<T> targetEnum) {
        try {
            return (T) targetEnum.getMethod("forValue", String.class).invoke(null, rawValue);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | SecurityException ex) {
            return null;
        }
    }

    @Nullable public <T extends Enum<T>> EnumSet<T> getEnumSetValue(@Nonnull final Class<T> targetEnum) {
        final String rawValue = this.getStringValue();
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        final EnumSet<T> result = EnumSet.noneOf(targetEnum);
        final String[] rawValues = rawValue.split(",");
        for (final String rawValueItem : rawValues) {
            final T value = getEnumValueInt(rawValueItem, targetEnum);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private <T extends Parsable> void assignFieldValues(
            final T item, final Map<String, Consumer<ParseNode>> fieldDeserializers) {
        if (currentNode.isJsonObject()) {
            if (this.onBeforeAssignFieldValues != null) {
                this.onBeforeAssignFieldValues.accept(item);
            }
            Map<String, Object> itemAdditionalData = null;
            if (item instanceof AdditionalDataHolder) {
                itemAdditionalData = ((AdditionalDataHolder) item).getAdditionalData();
            }
            for (final Map.Entry<String, JsonElement> fieldEntry :
                    currentNode.getAsJsonObject().entrySet()) {
                final String fieldKey = fieldEntry.getKey();
                final Consumer<ParseNode> fieldDeserializer = fieldDeserializers.get(fieldKey);
                final JsonElement fieldValue = fieldEntry.getValue();
                if (fieldValue.isJsonNull()) continue;
                if (fieldDeserializer != null) {
                    final JsonParseNode itemNode = new JsonParseNode(fieldValue);
                    itemNode.setOnBeforeAssignFieldValues(this.onBeforeAssignFieldValues);
                    itemNode.setOnAfterAssignFieldValues(this.onAfterAssignFieldValues);
                    fieldDeserializer.accept(itemNode);
                } else if (itemAdditionalData != null)
                    itemAdditionalData.put(fieldKey, this.tryGetAnything(fieldValue));
            }
            if (this.onAfterAssignFieldValues != null) {
                this.onAfterAssignFieldValues.accept(item);
            }
        }
    }

    private Object tryGetAnything(final JsonElement element) {
        if (element.isJsonNull()) return null;
        else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            else if (primitive.isString()) return primitive.getAsString();
            else if (primitive.isNumber()) return primitive.getAsDouble();
            else
                throw new RuntimeException(
                        "Could not get the value during deserialization, unknown primitive type");
        } else if (element.isJsonObject() || element.isJsonArray()) return element;
        else
            throw new RuntimeException(
                    "Could not get the value during deserialization, unknown primitive type");
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
