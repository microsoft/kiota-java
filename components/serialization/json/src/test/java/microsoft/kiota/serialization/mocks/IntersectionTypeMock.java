package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.microsoft.kiota.serialization.Parsable;

public class IntersectionTypeMock implements Parsable {
	private TestEntity _composedType1;
    private SecondTestEntity _composedType2;
    private String _stringValue;
    private java.util.List<TestEntity> _composedType3;
	@javax.annotation.Nonnull
    public static IntersectionTypeMock createFromDiscriminatorValue(@javax.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new IntersectionTypeMock();
    }

	@Override
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		// TODO Auto-generated method stub
        return new HashMap<>();
	}

	@Override
	public void serialize(SerializationWriter writer) {
        Objects.requireNonNull(writer);
		// TODO Auto-generated method stub
		
	}
	public TestEntity getComposedType1() {
        return _composedType1;
    }

    public void setComposedType1(TestEntity value) {
        this._composedType1 = value;
    }

    public SecondTestEntity getComposedType2() {
        return _composedType2;
    }

    public void setComposedType2(SecondTestEntity value) {
        this._composedType2 = value;
    }

    public String getStringValue() {
        return _stringValue;
    }

    public void setStringValue(String value) {
        this._stringValue = value;
    }

    public java.util.List<TestEntity> getComposedType3() {
        return _composedType3;
    }

    public void setComposedType3(java.util.List<TestEntity> value) {
        this._composedType3 = value;
    }	
}
