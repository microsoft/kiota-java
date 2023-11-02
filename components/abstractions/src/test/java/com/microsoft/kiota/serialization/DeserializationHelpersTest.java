package com.microsoft.kiota.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.microsoft.kiota.serialization.mocks.TestEntity;

class DeserializationHelpersTest {
	private final static String _jsonContentType = "application/json";
	private final static String _charset = "utf-8";
	@Test
	void defensive() {
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserialize(null, (InputStream)null, TestEntity::createFromDiscriminatorValue));
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserialize(_jsonContentType, (InputStream)null, TestEntity::createFromDiscriminatorValue));
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserialize(_jsonContentType, new ByteArrayInputStream("{}".getBytes(_charset)), (ParsableFactory<TestEntity>)null));
	}
	@Test
	void defensiveCollection() {
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserializeCollection(null, (InputStream)null, TestEntity::createFromDiscriminatorValue));
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserializeCollection(_jsonContentType, (InputStream)null, TestEntity::createFromDiscriminatorValue));
		assertThrows(NullPointerException.class, () -> KiotaSerialization.deserializeCollection(_jsonContentType, new ByteArrayInputStream("{}".getBytes(_charset)), (ParsableFactory<TestEntity>)null));
	}
	@Test
	void deserializesObjectWithoutReflection() throws IOException {
		final String strValue = "{'id':'123'}";
		final ParseNode mockParseNode = mock(ParseNode.class);
		when(mockParseNode.getObjectValue(any())).thenReturn(new TestEntity() {{
			setId("123");
		}});
		final ParseNodeFactory mockParseNodeFactory = mock(ParseNodeFactory.class);
		when(mockParseNodeFactory.getParseNode(any(), any())).thenReturn(mockParseNode);
		ParseNodeFactoryRegistry.defaultInstance.contentTypeAssociatedFactories.put(_jsonContentType, mockParseNodeFactory);

		final var result = KiotaSerialization.deserialize(_jsonContentType, strValue, TestEntity::createFromDiscriminatorValue);
		assertEquals("123", result.getId());
	}
	@Test
	void deserializesObjectWithReflection() throws IOException {
		final String strValue = "{'id':'123'}";
		final ParseNode mockParseNode = mock(ParseNode.class);
		when(mockParseNode.getObjectValue(any())).thenReturn(new TestEntity() {{
			setId("123");
		}});
		final ParseNodeFactory mockParseNodeFactory = mock(ParseNodeFactory.class);
		when(mockParseNodeFactory.getParseNode(any(), any())).thenReturn(mockParseNode);
		ParseNodeFactoryRegistry.defaultInstance.contentTypeAssociatedFactories.put(_jsonContentType, mockParseNodeFactory);

		final var result = KiotaSerialization.deserialize(_jsonContentType, strValue, TestEntity.class);
		assertEquals("123", result.getId());
	}
	@Test
	void deserializesCollectionOfObjects() throws IOException {
		final String strValue = "{'id':'123'}";
		final ParseNode mockParseNode = mock(ParseNode.class);
		when(mockParseNode.getCollectionOfObjectValues(any())).thenReturn(
		new ArrayList<Parsable>() {{
			add(new TestEntity() {{
				setId("123");
			}});
		}});	
		final ParseNodeFactory mockParseNodeFactory = mock(ParseNodeFactory.class);
		when(mockParseNodeFactory.getParseNode(any(), any())).thenReturn(mockParseNode);
		ParseNodeFactoryRegistry.defaultInstance.contentTypeAssociatedFactories.put(_jsonContentType, mockParseNodeFactory);

		final var result = KiotaSerialization.deserializeCollection(_jsonContentType, strValue, TestEntity::createFromDiscriminatorValue);
		assertEquals("123", result.get(0).getId());
	}
	
}
