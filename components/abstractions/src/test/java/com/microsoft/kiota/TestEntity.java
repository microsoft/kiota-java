package com.microsoft.kiota;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.SerializationWriter;

public class TestEntity implements Parsable {

	@Override
	@Nonnull
	public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void serialize(@Nonnull SerializationWriter writer) {
		// TODO Auto-generated method stub
		
	}
	
}
