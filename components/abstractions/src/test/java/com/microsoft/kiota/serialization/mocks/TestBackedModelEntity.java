package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.AdditionalDataHolder;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.store.BackedModel;
import com.microsoft.kiota.store.BackingStore;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

import jakarta.annotation.Nonnull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TestBackedModelEntity implements Parsable, AdditionalDataHolder, BackedModel {
    private BackingStore backingStore;

    public TestBackedModelEntity() {
        backingStore = BackingStoreFactorySingleton.instance.createBackingStore();
    }

    public String getId() {
        return this.backingStore.get("id");
    }

    public void setId(String _id) {
        this.backingStore.set("id", _id);
    }

    public String getOfficeLocation() {
        return this.backingStore.get("officeLocation");
    }

    public void setOfficeLocation(String _officeLocation) {
        this.backingStore.set("officeLocation", _officeLocation);
    }

    public LocalDate getBirthDay() {
        return this.backingStore.get("birthDay");
    }

    public void setBirthDay(LocalDate value) {
        this.backingStore.set("birthDay", value);
    }

    public PeriodAndDuration getWorkDuration() {
        return this.backingStore.get("workDuration");
    }

    public void setWorkDuration(PeriodAndDuration value) {
        this.backingStore.set("workDuration", PeriodAndDuration.ofPeriodAndDuration(value));
    }

    public LocalTime getStartWorkTime() {
        return this.backingStore.get("startWorkTime");
    }

    public void setStartWorkTime(LocalTime value) {
        this.backingStore.set("startWorkTime", value);
    }

    public LocalTime getEndWorkTime() {
        return this.backingStore.get("endWorkTime");
    }

    public void setEndWorkTime(LocalTime value) {
        this.backingStore.set("endWorkTime", value);
    }

    public OffsetDateTime getCreatedDateTime() {
        return this.backingStore.get("createdDateTime");
    }

    public void setCreatedDateTime(OffsetDateTime value) {
        this.backingStore.set("createdDateTime", value);
    }

    @Nonnull @Override
    public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        return new HashMap<>() {
            {
                put(
                        "id",
                        (n) -> {
                            setId(n.getStringValue());
                        });
                put(
                        "officeLocation",
                        (n) -> {
                            setOfficeLocation(n.getStringValue());
                        });
                put(
                        "birthDay",
                        (n) -> {
                            setBirthDay(n.getLocalDateValue());
                        });
                put(
                        "workDuration",
                        (n) -> {
                            setWorkDuration(n.getPeriodAndDurationValue());
                        });
                put(
                        "startWorkTime",
                        (n) -> {
                            setStartWorkTime(n.getLocalTimeValue());
                        });
                put(
                        "endWorkTime",
                        (n) -> {
                            setEndWorkTime(n.getLocalTimeValue());
                        });
                put(
                        "createdDateTime",
                        (n) -> {
                            setCreatedDateTime(n.getOffsetDateTimeValue());
                        });
            }
        };
    }

    @Override
    public void serialize(@Nonnull SerializationWriter writer) {
        Objects.requireNonNull(writer);
        writer.writeStringValue("id", getId());
        writer.writeStringValue("officeLocation", getOfficeLocation());
        writer.writeLocalDateValue("birthDay", getBirthDay());
        writer.writePeriodAndDurationValue("workDuration", getWorkDuration());
        writer.writeLocalTimeValue("startWorkTime", getStartWorkTime());
        writer.writeLocalTimeValue("endWorkTime", getEndWorkTime());
        writer.writeOffsetDateTimeValue("createdDateTime", getCreatedDateTime());
        writer.writeAdditionalData(getAdditionalData());
    }

    @Nonnull @Override
    public Map<String, Object> getAdditionalData() {
        Map<String, Object> value = this.backingStore.get("additionalData");
        if (value == null) {
            value = new HashMap<>();
            this.setAdditionalData(value);
        }
        return value;
    }

    /**
     * Sets the AdditionalData property value. Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.
     * @param value Value to set for the AdditionalData property.
     */
    public void setAdditionalData(@jakarta.annotation.Nullable final Map<String, Object> value) {
        this.backingStore.set("additionalData", value);
    }

    @jakarta.annotation.Nonnull public static TestEntity createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        return new TestEntity();
    }

    @Override
    public BackingStore getBackingStore() {
        return backingStore;
    }
}
