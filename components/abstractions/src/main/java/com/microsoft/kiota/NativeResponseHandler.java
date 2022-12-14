package com.microsoft.kiota;

import java.util.HashMap;
import java.util.Objects;
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
    
    public void setErrorMappings(HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        this.errorMappings = new HashMap<>(errorMappings);
    }

    public HashMap<String, ParsableFactory<? extends Parsable>> getErrorMappings() {
        return new HashMap<>(this.errorMappings);
    }
    
    public Object getValue() {
        return this.value;
    }
}