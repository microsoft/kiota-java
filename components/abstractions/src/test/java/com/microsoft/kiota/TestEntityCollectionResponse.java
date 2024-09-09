package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestEntityCollectionResponse extends BaseCollectionPaginationCountResponse
        implements Parsable {

    /**
     * Instantiates a new {@link TestEntityCollectionResponse} and sets the default values.
     */
    public TestEntityCollectionResponse() {
        super();
    }

    /**
     * Creates a new instance of the appropriate class based on discriminator value
     * @param parseNode The parse node to use to read the discriminator value and create the object
     * @return a {@link TestEntityCollectionResponse}
     */
    @jakarta.annotation.Nonnull public static TestEntityCollectionResponse createFromDiscriminatorValue(
            @jakarta.annotation.Nonnull final ParseNode parseNode) {
        Objects.requireNonNull(parseNode);
        return new TestEntityCollectionResponse();
    }

    /**
     * The deserialization information for the current model
     * @return a {@link Map<String, java.util.function.Consumer<ParseNode>>}
     */
    @Override
    @jakarta.annotation.Nonnull public Map<String, java.util.function.Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>(
                        super.getFieldDeserializers());
        deserializerMap.put(
                "value",
                n -> {
                    this.setValue(
                            n.getCollectionOfObjectValues(
                                    TestEntity::createFromDiscriminatorValue));
                });
        return deserializerMap;
    }

    /**
     * Gets the value property value. The value property
     * @return a {@link java.util.List<TestEntity>}
     */
    @jakarta.annotation.Nullable public java.util.List<TestEntity> getValue() {
        return this.backingStore.get("value");
    }

    /**
     * Serializes information the current object
     * @param writer Serialization writer to use to serialize this model
     */
    @Override
    public void serialize(@jakarta.annotation.Nonnull final SerializationWriter writer) {
        Objects.requireNonNull(writer);
        super.serialize(writer);
        writer.writeCollectionOfObjectValues("value", this.getValue());
    }

    /**
     * Sets the value property value. The value property
     * @param value Value to set for the value property.
     */
    public void setValue(@jakarta.annotation.Nullable final java.util.List<TestEntity> value) {
        this.backingStore.set("value", value);
    }
}
