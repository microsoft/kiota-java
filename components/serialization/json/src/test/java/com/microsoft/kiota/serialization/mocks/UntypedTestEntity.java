package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class UntypedTestEntity implements Parsable, AdditionalDataHolder {
    private String _id;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    private UntypedNode _location;

    public UntypedNode getLocation() {
        return _location;
    }

    public void setLocation(UntypedNode _location) {
        this._location = _location;
    }

    private UntypedNode _keywords;

    public UntypedNode getKeywords() {
        return _keywords;
    }

    public void setKeywords(UntypedNode _keywords) {
        this._keywords = _keywords;
    }

    private UntypedNode _detail;

    public UntypedNode getDetail() {
        return _detail;
    }

    public void setDetail(UntypedNode _detail) {
        this._detail = _detail;
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
                        "location",
                        (n) -> {
                            setLocation(
                                    n.getObjectValue(UntypedNode::createFromDiscriminatorValue));
                        });
                put(
                        "keywords",
                        (n) -> {
                            setKeywords(
                                    n.getObjectValue(UntypedNode::createFromDiscriminatorValue));
                        });
                put(
                        "detail",
                        (n) -> {
                            setDetail(n.getObjectValue(UntypedNode::createFromDiscriminatorValue));
                        });
            }
        };
    }

    @Override
    public void serialize(SerializationWriter writer) {
        Objects.requireNonNull(writer);
        writer.writeStringValue("id", getId());
        writer.writeObjectValue("location", getLocation());
        writer.writeObjectValue("keywords", getDetail());
        writer.writeObjectValue("detail", getDetail());
        writer.writeAdditionalData(getAdditionalData());
    }

    private final Map<String, Object> _additionalData = new HashMap<>();

    @Override
    public Map<String, Object> getAdditionalData() {
        return _additionalData;
    }

    @jakarta.annotation.Nonnull public static UntypedTestEntity createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        return new UntypedTestEntity();
    }
}
