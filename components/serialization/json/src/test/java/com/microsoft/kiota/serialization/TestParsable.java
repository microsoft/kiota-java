package com.microsoft.kiota.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TestParsable<T> implements Parsable {
    @FunctionalInterface
    public interface WriteMethod<V> {
        public void write(SerializationWriter writer, String key, V value);
    }

    @FunctionalInterface
    public interface ParseMethod<V> {
        public V parse(ParseNode parseNode);
    }

    private T realValue;

    private T nullValue;

    private final ParseMethod<T> parseMethod;

    private final WriteMethod<T> writeMethod;

    public TestParsable(ParseMethod<T> parseMethod, WriteMethod<T> writeMethod) {
        this(parseMethod, writeMethod, null);
    }

    public TestParsable(ParseMethod<T> parseMethod, WriteMethod<T> writeMethod, T value) {
        this.parseMethod = parseMethod;
        this.writeMethod = writeMethod;
        this.realValue = value;
    }

    @Override
    public Map<String, Consumer<ParseNode>> getFieldDeserializers() {
        final HashMap<String, java.util.function.Consumer<ParseNode>> deserializerMap =
                new HashMap<String, java.util.function.Consumer<ParseNode>>(2);
        deserializerMap.put(
                "realValue",
                (n) -> {
                    this.realValue = parseMethod.parse(n);
                });
        deserializerMap.put(
                "nullValue",
                (n) -> {
                    this.nullValue = parseMethod.parse(n);
                });
        return deserializerMap;
    }

    @Override
    public void serialize(SerializationWriter writer) {
        writeMethod.write(writer, "realValue", realValue);
        writeMethod.write(writer, "nullValue", nullValue);
    }

    public T getRealValue() {
        return realValue;
    }

    public T getNullValue() {
        return nullValue;
    }

    public static <T> ParsableFactory<TestParsable<T>> factory(
            ParseMethod<T> parseMethod, WriteMethod<T> writeMethod) {
        return (n) -> new TestParsable<T>(parseMethod, writeMethod);
    }
}
