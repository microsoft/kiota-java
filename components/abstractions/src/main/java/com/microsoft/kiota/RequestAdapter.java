package com.microsoft.kiota;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.ValuedEnumParser;
import com.microsoft.kiota.store.BackingStoreFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

/** Service responsible for translating abstract Request Info into concrete native HTTP requests. */
public interface RequestAdapter {
    /**
     * Enables the backing store proxies for the SerializationWriters and ParseNodes in use.
     * @param backingStoreFactory The backing store factory to use.
     */
    void enableBackingStore(@Nullable final BackingStoreFactory backingStoreFactory);

    /**
     * Gets the serialization writer factory currently in use for the HTTP core service.
     * @return the serialization writer factory currently in use for the HTTP core service.
     */
    @Nonnull SerializationWriterFactory getSerializationWriterFactory();

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized response model.
     * @param requestInfo the request info to execute.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param factory the factory to create the parsable object from the type discriminator.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized response model.
     */
    @Nullable <ModelType extends Parsable> ModelType send(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ParsableFactory<ModelType> factory);

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized response model collection.
     * @param requestInfo the request info to execute.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param factory the factory to create the parsable object from the type discriminator.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized response model collection.
     */
    @Nullable <ModelType extends Parsable> List<ModelType> sendCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ParsableFactory<ModelType> factory);

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized primitive response model.
     * @param requestInfo the request info to execute.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param targetClass the class of the response model to deserialize the response into.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized primitive response model.
     */
    @Nullable <ModelType> ModelType sendPrimitive(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final Class<ModelType> targetClass);

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized primitive collection response model.
     * @param requestInfo the request info to execute.
     * @param targetClass the class of the response model to deserialize the response into.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized primitive collection response model.
     */
    @Nullable <ModelType> List<ModelType> sendPrimitiveCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final Class<ModelType> targetClass);

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized enum value.
     * @param requestInfo the request info to execute.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param enumParser a parser from string to enum instances.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized primitive response model.
     */
    @Nullable <ModelType extends Enum<ModelType>> ModelType sendEnum(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ValuedEnumParser<ModelType> enumParser);

    /**
     * Executes the HTTP request specified by the given RequestInformation and returns the deserialized enum collection value.
     * @param requestInfo the request info to execute.
     * @param errorMappings the error factories mapping to use in case of a failed request.
     * @param enumParser a parser from string to enum instances.
     * @param <ModelType> the type of the response model to deserialize the response into.
     * @return the deserialized primitive response model.
     */
    @Nullable <ModelType extends Enum<ModelType>> List<ModelType> sendEnumCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ValuedEnumParser<ModelType> enumParser);

    /**
     * Sets The base url for every request.
     * @param baseUrl The base url for every request.
     */
    void setBaseUrl(@Nonnull final String baseUrl);

    /**
     * Gets The base url for every request.
     * @return The base url for every request.
     */
    @Nonnull String getBaseUrl();

    /**
     * Converts the given RequestInformation into a native HTTP request.
     * @param <T> the type of the native HTTP request.
     * @param requestInfo the request info to convert.
     * @return the native HTTP request.
     */
    @Nonnull <T> T convertToNativeRequest(@Nonnull final RequestInformation requestInfo);
}
