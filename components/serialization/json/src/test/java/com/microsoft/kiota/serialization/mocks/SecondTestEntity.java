package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.AdditionalDataHolder;

public class SecondTestEntity implements Parsable, AdditionalDataHolder {
	private String _displayName;
	private Integer _id;
	private Long _failureRate;

	@Override
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		return new HashMap<>(){{
			put("displayName", (n) -> setDisplayName(n.getStringValue()));
			put("id", (n) -> setId(n.getIntegerValue()));
			put("failureRate", (n) -> setFailureRate(n.getLongValue()));
		}};
	}

	@Override
	public void serialize(SerializationWriter writer) {
		Objects.requireNonNull(writer);

		writer.writeStringValue("displayName", getDisplayName());
		writer.writeIntegerValue("id", getId());
		writer.writeLongValue("failureRate", getFailureRate());
		writer.writeAdditionalData(getAdditionalData());
	}
	private final Map<String, Object> _additionalData = new HashMap<>();
	@Override
	public Map<String, Object> getAdditionalData() {
		return _additionalData;
	}

	public String getDisplayName() {
		return _displayName;
	}

	public void setDisplayName(String value) {
		this._displayName = value;
	}

	public Integer getId() {
		return _id;
	}

	public void setId(Integer value) {
		this._id = value;
	}

	public Long getFailureRate() {
		return _failureRate;
	}

	public void setFailureRate(Long value) {
		this._failureRate = value;
	}
	@jakarta.annotation.Nonnull
    public static SecondTestEntity createFromDiscriminatorValue(@jakarta.annotation.Nonnull final ParseNode parseNode) {
		return new SecondTestEntity();
	}	
}
