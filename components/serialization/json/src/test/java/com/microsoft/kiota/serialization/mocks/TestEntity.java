package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.AdditionalDataHolder;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TestEntity implements Parsable, AdditionalDataHolder {
    private String _id;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    private List<String> _phones;

    public List<String> getPhones() {
        return _phones;
    }

    public void setPhones(List<String> _phones) {
        this._phones = new ArrayList<String>(_phones);
    }

    private String _officeLocation;

    public String getOfficeLocation() {
        return _officeLocation;
    }

    public void setOfficeLocation(String _officeLocation) {
        this._officeLocation = _officeLocation;
    }

    private LocalDate _birthDay;

    public LocalDate getBirthDay() {
        return _birthDay;
    }

    public void setBirthDay(LocalDate value) {
        this._birthDay = value;
    }

    private PeriodAndDuration _workDuration;

    public PeriodAndDuration getWorkDuration() {
        return _workDuration;
    }

    public void setWorkDuration(PeriodAndDuration value) {
        this._workDuration = PeriodAndDuration.ofPeriodAndDuration(value);
    }

    private LocalTime _startWorkTime;

    public LocalTime getStartWorkTime() {
        return _startWorkTime;
    }

    public void setStartWorkTime(LocalTime value) {
        this._startWorkTime = value;
    }

    private LocalTime _endWorkTime;

    public LocalTime getEndWorkTime() {
        return _endWorkTime;
    }

    public void setEndWorkTime(LocalTime value) {
        this._endWorkTime = value;
    }

    private MyEnum _myEnum;

    public MyEnum getMyEnum() {
        return _myEnum;
    }

    public void setMyEnum(MyEnum value) {
        this._myEnum = value;
    }

    private List<MyEnum> _enumCollection;

    public List<MyEnum> getEnumCollection() {
        return _enumCollection;
    }

    public void setEnumCollection(List<MyEnum> value) {
        this._enumCollection = new ArrayList<MyEnum>(value);
    }

    private OffsetDateTime _createdDateTime;

    public OffsetDateTime getCreatedDateTime() {
        return _createdDateTime;
    }

    public void setCreatedDateTime(OffsetDateTime value) {
        this._createdDateTime = value;
    }

    @Override
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
                        "myEnum",
                        (n) -> {
                            setMyEnum(n.getEnumValue(MyEnum::forValue));
                        });
                put(
                        "enumCollection",
                        (n) -> {
                            setEnumCollection(n.getCollectionOfEnumValues(MyEnum::forValue));
                        });
                put(
                        "createdDateTime",
                        (n) -> {
                            setCreatedDateTime(n.getOffsetDateTimeValue());
                        });
                put(
                        "phones",
                        (n) -> {
                            setPhones(n.getCollectionOfPrimitiveValues(String.class));
                        });
            }
        };
    }

    @Override
    public void serialize(SerializationWriter writer) {
        Objects.requireNonNull(writer);
        writer.writeStringValue("id", getId());
        writer.writeStringValue("officeLocation", getOfficeLocation());
        writer.writeLocalDateValue("birthDay", getBirthDay());
        writer.writePeriodAndDurationValue("workDuration", getWorkDuration());
        writer.writeLocalTimeValue("startWorkTime", getStartWorkTime());
        writer.writeLocalTimeValue("endWorkTime", getEndWorkTime());
        writer.writeEnumValue("myEnum", getMyEnum());
        writer.writeCollectionOfEnumValues("enumCollection", getEnumCollection());
        writer.writeOffsetDateTimeValue("createdDateTime", getCreatedDateTime());
        writer.writeCollectionOfPrimitiveValues("phones", getPhones());
        writer.writeAdditionalData(getAdditionalData());
    }

    private final Map<String, Object> _additionalData = new HashMap<>();

    @Override
    public Map<String, Object> getAdditionalData() {
        return _additionalData;
    }

    @jakarta.annotation.Nonnull public static TestEntity createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        return new TestEntity();
    }
}
