package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TestEntity implements Parsable {

    @Override
    @Nonnull public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        return new HashMap<String, Consumer<ParseNode>>();
    }

    @Override
    public void serialize(@Nonnull SerializationWriter writer) {
        // TODO Auto-generated method stub

    }
}
