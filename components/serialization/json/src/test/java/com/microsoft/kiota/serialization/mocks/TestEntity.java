package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.PeriodAndDuration;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.AdditionalDataHolder;

public class TestEntity implements Parsable, AdditionalDataHolder {
	private String _id;
	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
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
		this._workDuration = value;
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

	//TODO enum
	private OffsetDateTime _createdDateTime;

	public OffsetDateTime getCreatedDateTime() {
		return _createdDateTime;
	}

	public void setCreatedDateTime(OffsetDateTime value) {
		this._createdDateTime = value;
	}

	@Override
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		return new HashMap<>() {{
			put("id", (n) -> {
				setId(n.getStringValue());
			});
			put("officeLocation", (n) -> {
				setOfficeLocation(n.getStringValue());
			});
			put("birthDay", (n) -> {
				setBirthDay(n.getLocalDateValue());
			});
			put("workDuration", (n) -> {
				setWorkDuration(n.getPeriodAndDurationValue());
			});
			put("startWorkTime", (n) -> {
				setStartWorkTime(n.getLocalTimeValue());
			});
			put("endWorkTime", (n) -> {
				setEndWorkTime(n.getLocalTimeValue());
			});
			put("createdDateTime", (n) -> {
				setCreatedDateTime(n.getOffsetDateTimeValue());
			});
		}};
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
		writer.writeOffsetDateTimeValue("createdDateTime", getCreatedDateTime());
		writer.writeAdditionalData(getAdditionalData());
	}

	private final Map<String, Object> _additionalData = new HashMap<>();

	@Override
	public Map<String, Object> getAdditionalData() {
		return _additionalData;
	}
	@jakarta.annotation.Nonnull
    public static TestEntity createFromDiscriminatorValue(@jakarta.annotation.Nonnull final ParseNode parseNode) {
		return new TestEntity();
	}
}
