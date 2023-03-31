package com.microsoft.kiota;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
public class NativeResponseHandler implements ResponseHandler {
    private Object value;
    private HashMap<String, ParsableFactory<? extends Parsable>> errorMappings;
    @Override
    @Nonnull
    public <NativeResponseType, ModelType> CompletableFuture<ModelType> handleResponseAsync(@Nonnull NativeResponseType response, @Nullable HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        this.value = response;
        if(errorMappings != null)
            this.errorMappings = new HashMap<>(errorMappings);
        return CompletableFuture.completedFuture(null);
    }
    /**
     * Set the error mappings for the response to use when deserializing failed responses bodies.
     * @param errorMappings The designated error mappings.
     */
    public void setErrorMappings(Map<String, ParsableFactory<? extends Parsable>> errorMappings) {
        this.errorMappings = new HashMap<>(errorMappings);
    }
    /**
     * Get the error mappings for the response when deserializing failed response bodies.
     * @return The error mappings for failed response bodies.
     */
    public Map<String, ParsableFactory<? extends Parsable>> getErrorMappings() {
        return new HashMap<>(this.errorMappings);
    }
    /**
     * Get the value for the response.
     * @return the value of the response.
     */
    public Object getValue() {
        return this.value;
    }
}