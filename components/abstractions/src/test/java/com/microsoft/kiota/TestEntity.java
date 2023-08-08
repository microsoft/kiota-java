package com.microsoft.kiota;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import jakarta.annotation.Nonnull;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

public class TestEntity implements Parsable {

	@Override
	@Nonnull
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		return new HashMap<String, Consumer<ParseNode>>();
	}

	@Override
	public void serialize(@Nonnull SerializationWriter writer) {
		// TODO Auto-generated method stub
		
	}
	
}
