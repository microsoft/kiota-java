package com.microsoft.kiota.serialization.mocks;

import com.microsoft.kiota.serialization.ValuedEnum;
import java.util.Objects;

public enum TestEnum implements ValuedEnum {
    First("1"),
    Second("2");
    public final String value;

    TestEnum(final String value) {
        this.value = value;
    }

    @jakarta.annotation.Nonnull
    public String getValue() {
        return this.value;
    }

    @jakarta.annotation.Nullable public static TestEnum forValue(@jakarta.annotation.Nonnull final String searchValue) {
        Objects.requireNonNull(searchValue);
        switch (searchValue) {
            case "1":
                return First;
            case "2":
                return Second;
            default:
                return null;
        }
    }
}
