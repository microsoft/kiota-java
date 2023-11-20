package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.ValuedEnum;
import jakarta.annotation.Nonnull;

import java.util.Objects;

public enum MyEnum implements ValuedEnum {
    MY_VALUE1("VALUE1"),
    MY_VALUE2("VALUE2");
    public final String value;
    MyEnum(final String value) {
        this.value = value;
    }
    @jakarta.annotation.Nonnull
    public String getValue() { return this.value; }
    @jakarta.annotation.Nullable
    public static MyEnum forValue(@jakarta.annotation.Nonnull final String searchValue) {
        Objects.requireNonNull(searchValue);
        switch(searchValue) {
            case "VALUE1": return MY_VALUE1;
            case "VALUE2": return MY_VALUE2;
            default: return null;
        }
    }
}
