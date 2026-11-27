package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.ValuedEnum;

import jakarta.annotation.Nonnull;

public enum TestEnum implements ValuedEnum {
    VALUE1("val1"),
    VALUE2("val2");

    private final String value;

    TestEnum(final String value) {
        this.value = value;
    }

    @Nonnull @Override
    public String getValue() {
        return this.value;
    }
}
