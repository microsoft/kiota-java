package com.microsoft.kiota.serialization.mocks;

import com.google.gson.JsonSyntaxException;
import com.microsoft.kiota.serialization.ComposedTypeWrapper;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class UnionTypeMock implements Parsable, ComposedTypeWrapper {
    private TestEntity _composedType1;
    private SecondTestEntity _composedType2;
    private String _stringValue;
    private java.util.List<TestEntity> _composedType3;

    @jakarta.annotation.Nonnull public static UnionTypeMock createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        final UnionTypeMock result = new UnionTypeMock();
        final ParseNode mappingValueNode = parseNode.getChildNode("@odata.type");
        if (mappingValueNode != null) {
            final String mappingValue = mappingValueNode.getStringValue();
            if ("#microsoft.graph.testEntity".equalsIgnoreCase(mappingValue)) {
                result.setComposedType1(new TestEntity());
            } else if ("#microsoft.graph.secondTestEntity".equalsIgnoreCase(mappingValue)) {
                result.setComposedType2(new SecondTestEntity());
            }
        } else {
            try {
                result.setStringValue(parseNode.getStringValue());
            } catch (JsonSyntaxException e) {
                if (parseNode.getCollectionOfObjectValues(TestEntity::createFromDiscriminatorValue)
                        != null) {
                    result.setComposedType3(
                            parseNode.getCollectionOfObjectValues(
                                    TestEntity::createFromDiscriminatorValue));
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        if (getComposedType1() != null) return getComposedType1().getFieldDeserializers();
        else if (getComposedType2() != null) return getComposedType2().getFieldDeserializers();
        return new HashMap<>();
    }

    @Override
    public void serialize(SerializationWriter writer) {
        Objects.requireNonNull(writer);
        if (getComposedType1() != null) writer.writeObjectValue(null, getComposedType1());
        else if (getComposedType2() != null) writer.writeObjectValue(null, getComposedType2());
        else if (getStringValue() != null) writer.writeStringValue(null, getStringValue());
        else if (getComposedType3() != null)
            writer.writeCollectionOfObjectValues(null, getComposedType3());
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
