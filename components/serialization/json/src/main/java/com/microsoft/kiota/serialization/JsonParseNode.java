package com.microsoft.kiota.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.kiota.PeriodAndDuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/** ParseNode implementation for JSON */
public class JsonParseNode implements ParseNode {
    private final JsonElement currentNode;
    protected final Gson gson;

    /**
     * Creates a new instance of the JsonParseNode class.
     * @param node the node to wrap.
     * @deprecated use {@link #JsonParseNode(JsonElement, Gson)} instead.
     */
    @Deprecated
    public JsonParseNode(@Nonnull final JsonElement node) {
        currentNode = Objects.requireNonNull(node, "parameter node cannot be null");
        this.gson = DefaultGsonBuilder.getDefaultInstance();
    }

    /**
     * Creates a new instance of the JsonParseNode class.
     * @param node the node to wrap.
     * @param node the node to wrap.
     */
    public JsonParseNode(@Nonnull final JsonElement node, @Nonnull final Gson gson) {
        currentNode = Objects.requireNonNull(node, "parameter node cannot be null");
        this.gson = Objects.requireNonNull(gson, "parameter gson cannot be null");
    }

    /**
     * Creates a new {@link JsonParseNode} for the given {@link JsonElement}.
     * @param node the node to wrap.
     * @return the newly created {@link JsonParseNode}.
     */
    @Nonnull protected JsonParseNode createNewNode(@Nonnull JsonElement node) {
        return new JsonParseNode(node, gson);
    }

    /** {@inheritDoc} */
    @Nullable public ParseNode getChildNode(@Nonnull final String identifier) {
        Objects.requireNonNull(identifier, "identifier parameter is required");
        if (currentNode.isJsonObject()) {
            final JsonObject object = currentNode.getAsJsonObject();
            final JsonElement childNodeElement = object.get(identifier);
            if (childNodeElement == null) return null;
            final JsonParseNode result = createNewNode(childNodeElement);
            result.setOnBeforeAssignFieldValues(this.onBeforeAssignFieldValues);
            result.setOnAfterAssignFieldValues(this.onAfterAssignFieldValues);
            return result;
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
        return gson.fromJson(currentNode, UUID.class);
    }

    @Nullable public OffsetDateTime getOffsetDateTimeValue() {
        return gson.fromJson(currentNode, OffsetDateTime.class);
    }

    @Nullable public LocalDate getLocalDateValue() {
        return gson.fromJson(currentNode, LocalDate.class);
    }

    @Nullable public LocalTime getLocalTimeValue() {
        return gson.fromJson(currentNode, LocalTime.class);
    }

    @Nullable public PeriodAndDuration getPeriodAndDurationValue() {
        return gson.fromJson(currentNode, PeriodAndDuration.class);
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
        } else if (targetClass == Double.class) {
            return (T) itemNode.getDoubleValue();
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

    private <T> List<T> iterateOnArray(JsonElement jsonElement, Function<JsonParseNode, T> fn) {
        JsonArray array = jsonElement.getAsJsonArray();
        final Iterator<JsonElement> sourceIterator = array.iterator();
        final List<T> result = new ArrayList<>();
        while (sourceIterator.hasNext()) {
            final JsonElement item = sourceIterator.next();
            final JsonParseNode itemNode = createNewNode(item);
            itemNode.setOnBeforeAssignFieldValues(this.getOnBeforeAssignFieldValues());
            itemNode.setOnAfterAssignFieldValues(this.getOnAfterAssignFieldValues());
            result.add(fn.apply(itemNode));
        }
        return result;
    }

    @Nullable public <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass) {
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            return iterateOnArray(
                    currentNode, itemNode -> getPrimitiveValue(targetClass, itemNode));
        } else throw new RuntimeException("invalid state expected to have an array node");
    }

    @Nullable public <T extends Parsable> List<T> getCollectionOfObjectValues(
            @Nonnull final ParsableFactory<T> factory) {
        Objects.requireNonNull(factory, "parameter factory cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            return iterateOnArray(currentNode, itemNode -> itemNode.getObjectValue(factory));
        } else return null;
    }

    @Nullable public <T extends Enum<T>> List<T> getCollectionOfEnumValues(
            @Nonnull final ValuedEnumParser<T> enumParser) {
        Objects.requireNonNull(enumParser, "parameter enumParser cannot be null");
        if (currentNode.isJsonNull()) {
            return null;
        } else if (currentNode.isJsonArray()) {
            return iterateOnArray(currentNode, itemNode -> itemNode.getEnumValue(enumParser));
        } else throw new RuntimeException("invalid state expected to have an array node");
    }

    @Nonnull public <T extends Parsable> T getObjectValue(@Nonnull final ParsableFactory<T> factory) {
        Objects.requireNonNull(factory, "parameter factory cannot be null");
        final T item = factory.create(this);
        if (item.getClass() == UntypedNode.class) return (T) getUntypedValue();
        assignFieldValues(item, item.getFieldDeserializers());
        return item;
    }

    @Nonnull private UntypedNode getUntypedValue() {
        return getUntypedValue(currentNode);
    }

    @Nonnull private UntypedNode getUntypedValue(JsonElement element) {
        if (element.isJsonNull()) return new UntypedNull();
        else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) return new UntypedBoolean(primitive.getAsBoolean());
            else if (primitive.isString()) return new UntypedString(primitive.getAsString());
            else if (primitive.isNumber()) return new UntypedDouble(primitive.getAsDouble());
            else
                throw new RuntimeException(
                        "Could not get the value during deserialization, unknown primitive type");
        } else if (element.isJsonObject()) {
            HashMap<String, UntypedNode> propertiesMap = new HashMap<>();
            for (final Map.Entry<String, JsonElement> fieldEntry :
                    element.getAsJsonObject().entrySet()) {
                final String fieldKey = fieldEntry.getKey();
                final JsonElement fieldValue = fieldEntry.getValue();
                final JsonParseNode childNode = createNewNode(fieldValue);
                childNode.setOnBeforeAssignFieldValues(this.getOnBeforeAssignFieldValues());
                childNode.setOnAfterAssignFieldValues(this.getOnAfterAssignFieldValues());
                propertiesMap.put(fieldKey, childNode.getUntypedValue());
            }
            return new UntypedObject(propertiesMap);

        } else if (element.isJsonArray()) {
            return new UntypedArray(iterateOnArray(element, JsonParseNode::getUntypedValue));
        }

        throw new RuntimeException(
                "Could not get the value during deserialization, unknown json value type");
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
                    final JsonParseNode itemNode = createNewNode(fieldValue);
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
        } else if (element.isJsonObject() || element.isJsonArray()) return getUntypedValue(element);
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
