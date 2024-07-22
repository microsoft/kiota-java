package com.microsoft.kiota.bundle;

import com.microsoft.kiota.ApiClientBuilder;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.http.ObservabilityOptions;
import com.microsoft.kiota.http.OkHttpRequestAdapter;
import com.microsoft.kiota.serialization.FormParseNodeFactory;
import com.microsoft.kiota.serialization.FormSerializationWriterFactory;
import com.microsoft.kiota.serialization.JsonParseNodeFactory;
import com.microsoft.kiota.serialization.JsonSerializationWriterFactory;
import com.microsoft.kiota.serialization.MultipartSerializationWriterFactory;
import com.microsoft.kiota.serialization.ParseNodeFactory;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.TextParseNodeFactory;
import com.microsoft.kiota.serialization.TextSerializationWriterFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Call;

/** RequestAdapter implementation for Kiota Bundle */
public class KiotaRequestAdapter extends OkHttpRequestAdapter {
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, parse node factory, serialization writer factory, http client and observability options.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     * @param client the http client to use for sending requests.
     * @param observabilityOptions the observability options to use for sending requests.
     */
    public KiotaRequestAdapter(
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
