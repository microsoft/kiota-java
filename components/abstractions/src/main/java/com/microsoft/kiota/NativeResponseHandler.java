package com.microsoft.kiota;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;

public class NativeResponseHandler implements ResponseHandler {

    public Object value;

    public HashMap<String, ParsableFactory<? extends Parsable>> errorMappings;

    @Override
    @Nonnull
    public <NativeResponseType, ModelType> CompletableFuture<ModelType> handleResponseAsync(@Nonnull NativeResponseType response, @Nullable HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        value = response;
        this.errorMappings = errorMappings;
        return CompletableFuture.completedFuture(null);
    }
}