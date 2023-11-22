package com.microsoft.kiota.serialization;

import com.microsoft.kiota.PeriodAndDuration;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Interface for a deserialization node in a parse tree. This interface provides an abstraction layer over serialization formats, libraries and implementations.
 */
public interface ParseNode {
    /**
     * Gets a new parse node for the given identifier.
     * @param identifier the identifier of the current node property.
     * @return a new parse node for the given identifier.
     */
    @Nullable ParseNode getChildNode(@Nonnull final String identifier);

    /**
     * Gets the string value of the node.
     * @return the string value of the node.
     */
    @Nullable String getStringValue();

    /**
     * Gets the boolean value of the node.
     * @return the boolean value of the node.
     */
    @Nullable Boolean getBooleanValue();

    /**
     * Gets the byte value of the node.
     * @return the byte value of the node.
     */
    @Nullable Byte getByteValue();

    /**
     * Gets the short value of the node.
     * @return the short value of the node.
     */
    @Nullable Short getShortValue();

    /**
     * Gets the BigDecimal value of the node.
     * @return the BigDecimal value of the node.
     */
    @Nullable BigDecimal getBigDecimalValue();

    /**
     * Gets the Integer value of the node.
     * @return the Integer value of the node.
     */
    @Nullable Integer getIntegerValue();

    /**
     * Gets the Float value of the node.
     * @return the Float value of the node.
     */
    @Nullable Float getFloatValue();

    /**
     * Gets the Double value of the node.
     * @return the Double value of the node.
     */
    @Nullable Double getDoubleValue();

    /**
     * Gets the Long value of the node.
     * @return the Long value of the node.
     */
    @Nullable Long getLongValue();

    /**
     * Gets the UUID value of the node.
     * @return the UUID value of the node.
     */
    @Nullable UUID getUUIDValue();

    /**
     * Gets the OffsetDateTime value of the node.
     * @return the OffsetDateTime value of the node.
     */
    @Nullable OffsetDateTime getOffsetDateTimeValue();

    /**
     * Gets the LocalDate value of the node.
     * @return the LocalDate value of the node.
     */
    @Nullable LocalDate getLocalDateValue();

    /**
     * Gets the LocalTime value of the node.
     * @return the LocalTime value of the node.
     */
    @Nullable LocalTime getLocalTimeValue();

    /**
     * Gets the Period value of the node.
     * @return the Period value of the node.
     */
    @Nullable PeriodAndDuration getPeriodAndDurationValue();

    /**
     * Gets the Enum value of the node.
     * @return the Enum value of the node.
     * @param enumParser the parser for Enums
     * @param <T> the type of the enum.
     */
    @Nullable <T extends Enum<T>> T getEnumValue(@Nonnull final ValuedEnumParser<T> enumParser);

    /**
     * Gets the EnumSet value of the node.
     * @return the EnumSet value of the node.
     * @param enumParser the parser for Enums
     * @param <T> the type of the enum.
     */
    @Nullable <T extends Enum<T>> EnumSet<T> getEnumSetValue(@Nonnull final ValuedEnumParser<T> enumParser);

    /**
     * Gets the collection of primitive values of the node.
     * @return the collection of primitive values of the node.
     * @param targetClass the class of the class.
     * @param <T> the type of the primitive.
     */
    @Nullable <T> List<T> getCollectionOfPrimitiveValues(@Nonnull final Class<T> targetClass);

    /**
     * Gets the collection of object values of the node.
     * @param factory the factory to use to create the model object.
     * @return the collection of object values of the node.
     * @param <T> the type of the model object.
     */
    @Nullable <T extends Parsable> List<T> getCollectionOfObjectValues(
            @Nonnull final ParsableFactory<T> factory);

    /**
     * Gets the collection of Enum values of the node.
     * @return the collection of Enum values of the node.
     * @param <T> the type of the enum.
     * @param enumParser the parser for Enums
     */
    @Nullable <T extends Enum<T>> List<T> getCollectionOfEnumValues(
            @Nonnull final ValuedEnumParser<T> enumParser);

    /**
     * Gets the model object value of the node.
     * @param factory the factory to use to create the model object.
     * @return the model object value of the node.
     * @param <T> the type of the model object.
     */
    @Nonnull <T extends Parsable> T getObjectValue(@Nonnull final ParsableFactory<T> factory);

    /**
     * Gets the callback called before the node is deserialized.
     * @return the callback called before the node is deserialized.
     */
    @Nullable Consumer<Parsable> getOnBeforeAssignFieldValues();

    /**
     * Gets the callback called after the node is deserialized.
     * @return the callback called after the node is deserialized.
     */
    @Nullable Consumer<Parsable> getOnAfterAssignFieldValues();

    /**
     * Sets the callback called before the node is deserialized.
     * @param value the callback called before the node is deserialized.
     */
    void setOnBeforeAssignFieldValues(@Nullable final Consumer<Parsable> value);

    /**
     * Sets the callback called after the node is deserialized.
     * @param value the callback called after the node is deserialized.
     */
    void setOnAfterAssignFieldValues(@Nullable final Consumer<Parsable> value);

    /**
     * Gets the byte array value of the node.
     * @return The byte array value of the node.
     */
    @Nullable byte[] getByteArrayValue();
}
