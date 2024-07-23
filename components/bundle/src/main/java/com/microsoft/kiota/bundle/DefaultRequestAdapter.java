package com.microsoft.kiota.bundle;

import com.microsoft.kiota.ApiClientBuilder;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.http.ObservabilityOptions;
import com.microsoft.kiota.http.OkHttpRequestAdapter;
import com.microsoft.kiota.serialization.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Call;

/** RequestAdapter implementation for Kiota Bundle */
public class DefaultRequestAdapter extends OkHttpRequestAdapter {

    /**
     * Instantiates a DefaultRequestAdapter with the provided authentication provider.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     */
    public DefaultRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider) {
        this(authenticationProvider, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter with the provided authentication provider, and the parse node factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     */
    @SuppressWarnings("LambdaLast")
    public DefaultRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory) {
        this(authenticationProvider, parseNodeFactory, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter with the provided authentication provider, parse node factory, and the serialization writer factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     */
    @SuppressWarnings("LambdaLast")
    public DefaultRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory) {
        this(authenticationProvider, parseNodeFactory, serializationWriterFactory, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter with the provided authentication provider, parse node factory, serialization writer factory, and the http client.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     * @param client the http client to use for sending requests.
     */
    @SuppressWarnings("LambdaLast")
    public DefaultRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory,
            @Nullable final Call.Factory client) {
        this(authenticationProvider, parseNodeFactory, serializationWriterFactory, client, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter with the provided authentication provider, parse node factory, serialization writer factory, http client and observability options.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     * @param client the http client to use for sending requests.
     * @param observabilityOptions the observability options to use for sending requests.
     */
    @SuppressWarnings("LambdaLast")
    public DefaultRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory,
            @Nullable final Call.Factory client,
            @Nullable final ObservabilityOptions observabilityOptions) {
        super(
                authenticationProvider,
                parseNodeFactory,
                serializationWriterFactory,
                client,
                observabilityOptions);
        setupDefaults();
    }

    private void setupDefaults() {
        ApiClientBuilder.registerDefaultSerializer(JsonSerializationWriterFactory::new);
        ApiClientBuilder.registerDefaultSerializer(TextSerializationWriterFactory::new);
        ApiClientBuilder.registerDefaultSerializer(FormSerializationWriterFactory::new);
        ApiClientBuilder.registerDefaultSerializer(MultipartSerializationWriterFactory::new);
        ApiClientBuilder.registerDefaultDeserializer(JsonParseNodeFactory::new);
        ApiClientBuilder.registerDefaultDeserializer(FormParseNodeFactory::new);
        ApiClientBuilder.registerDefaultDeserializer(TextParseNodeFactory::new);
    }
}
