package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ResponseHandler} implementation to handle native response objects
 */
public class NativeResponseHandler implements ResponseHandler {
    /**
     * Default constructor
     */
    public NativeResponseHandler() {
        // default empty constructor
    }

    private Object value;
    private HashMap<String, ParsableFactory<? extends Parsable>> errorMappings;

    @Override
    public <NativeResponseType, ModelType> ModelType handleResponse(
            @Nonnull NativeResponseType response,
            @Nullable HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        this.value = response;
        if (errorMappings != null) this.errorMappings = new HashMap<>(errorMappings);
        return null;
    }

    /**
     * Set the error mappings for the response to use when deserializing failed responses bodies.
     * @param errorMappings The designated error mappings.
     */
    public void setErrorMappings(
            @Nonnull Map<String, ParsableFactory<? extends Parsable>> errorMappings) {
        this.errorMappings = new HashMap<>(errorMappings);
    }

    /**
     * Get the error mappings for the response when deserializing failed response bodies.
     * @return The error mappings for failed response bodies.
     */
    @Nonnull public Map<String, ParsableFactory<? extends Parsable>> getErrorMappings() {
        return new HashMap<>(this.errorMappings);
    }

    /**
     * Get the value for the response.
     * @return the value of the response.
     */
    @Nullable public Object getValue() {
        return this.value;
    }
}
