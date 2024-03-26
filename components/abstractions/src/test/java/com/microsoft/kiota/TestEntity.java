package com.microsoft.kiota;

import com.microsoft.kiota.serialization.AdditionalDataHolder;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;
import com.microsoft.kiota.store.BackedModel;
import com.microsoft.kiota.store.BackingStore;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TestEntity implements Parsable, AdditionalDataHolder, BackedModel {

    protected BackingStore backingStore;

    /**
     * Gets the backingStore property value. Stores model information.
     * @return a {@link BackingStore}
     */
    @jakarta.annotation.Nonnull public BackingStore getBackingStore() {
        return this.backingStore;
    }

    public String getOdataType() {
        return this.backingStore.get("@odata.type");
    }

    public void setOdataType(String odataType) {
        this.backingStore.set("@odata.type", odataType);
    }

    public String getId() {
        return this.backingStore.get("id");
    }

    public void setId(String _id) {
        this.backingStore.set("id", _id);
    }

    public List<String> getBusinessPhones() {
        return this.backingStore.get("businessPhones");
    }

    public void setBusinessPhones(List<String> _businessPhones) {
        this.backingStore.set("businessPhones", _businessPhones);
    }

    public TestEntity getManager() {
        return this.backingStore.get("manager");
    }

    public void setManager(TestEntity manager) {
        this.backingStore.set("manager", manager);
    }

    public List<TestEntity> getColleagues() {
        return this.backingStore.get("colleagues");
    }

    public void setColleagues(List<TestEntity> colleagues) {
        this.backingStore.set("colleagues", colleagues);
    }

    /**
     * Instantiates a new {@link TestEntity} and sets the default values.
     */
    public TestEntity() {
        this.backingStore = BackingStoreFactorySingleton.instance.createBackingStore();
        this.setAdditionalData(new HashMap<>());
        this.setOdataType("#microsoft.graph.testEntity");
    }

    /**
     * Gets the AdditionalData property value. Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.
     * @return a {@link Map<String, Object>}
     */
    @jakarta.annotation.Nonnull public Map<String, Object> getAdditionalData() {
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

    @Override
    @Nonnull public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        return new HashMap<String, Consumer<ParseNode>>();
    }

    @Override
    public void serialize(@Nonnull SerializationWriter writer) {
        // TODO Auto-generated method stub

    }
}
