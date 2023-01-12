package com.microsoft.kiota.http;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.ApiClientBuilder;
import com.microsoft.kiota.ApiException;
import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.RequestOption;
import com.microsoft.kiota.ResponseHandlerOption;
import com.microsoft.kiota.ResponseHandler;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.serialization.ParseNodeFactoryRegistry;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.ParseNodeFactory;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.SerializationWriterFactoryRegistry;
import com.microsoft.kiota.store.BackingStoreFactory;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Context;

/** RequestAdapter implementation for OkHttp */
public class OkHttpRequestAdapter implements com.microsoft.kiota.RequestAdapter {
    private final static String contentTypeHeaderKey = "Content-Type";
    private final OkHttpClient client;
    private final AuthenticationProvider authProvider;
    private final ObservabilityOptions obsOptions;
    private ParseNodeFactory pNodeFactory;
    private SerializationWriterFactory sWriterFactory;
    private String baseUrl = "";
    public void setBaseUrl(@Nonnull final String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }
    @Nonnull
    public String getBaseUrl() {
        return baseUrl;
    }
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     */
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider){
        this(authenticationProvider, null, null, null, null);
    }
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, and the parse node factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider, @Nullable final ParseNodeFactory parseNodeFactory) {
        this(authenticationProvider, parseNodeFactory, null, null, null);
    }
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, parse node factory, and the serialization writer factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider, @Nullable final ParseNodeFactory parseNodeFactory, @Nullable final SerializationWriterFactory serializationWriterFactory) {
        this(authenticationProvider, parseNodeFactory, serializationWriterFactory, null, null);
    }
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, parse node factory, serialization writer factory, and the http client.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     * @param client the http client to use for sending requests.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider, @Nullable final ParseNodeFactory parseNodeFactory, @Nullable final SerializationWriterFactory serializationWriterFactory, @Nullable final OkHttpClient client) {
        this(authenticationProvider, parseNodeFactory, serializationWriterFactory, client, null);
    }
    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, parse node factory, serialization writer factory, http client and observability options.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     * @param client the http client to use for sending requests.
     * @param observabilityOptions the observability options to use for sending requests.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider, @Nullable final ParseNodeFactory parseNodeFactory, @Nullable final SerializationWriterFactory serializationWriterFactory, @Nullable final OkHttpClient client, @Nullable final ObservabilityOptions observabilityOptions) {
        this.authProvider = Objects.requireNonNull(authenticationProvider, "parameter authenticationProvider cannot be null");
        if(client == null) {
            this.client = KiotaClientFactory.Create().build();
        } else {
            this.client = client;
        }
        if(parseNodeFactory == null) {
            pNodeFactory = ParseNodeFactoryRegistry.defaultInstance;
        } else {
            pNodeFactory = parseNodeFactory;
        }

        if(serializationWriterFactory == null) {
            sWriterFactory = SerializationWriterFactoryRegistry.defaultInstance;
        } else {
            sWriterFactory = serializationWriterFactory;
        }

        if (observabilityOptions == null) {
            obsOptions = new ObservabilityOptions();
        } else {
            obsOptions = observabilityOptions;
        }
    }
    @Nonnull
    public SerializationWriterFactory getSerializationWriterFactory() {
        return sWriterFactory;
    }
    public void enableBackingStore(@Nullable final BackingStoreFactory backingStoreFactory) {
        this.pNodeFactory = Objects.requireNonNull(ApiClientBuilder.enableBackingStoreForParseNodeFactory(pNodeFactory));
        this.sWriterFactory = Objects.requireNonNull(ApiClientBuilder.enableBackingStoreForSerializationWriterFactory(sWriterFactory));
        if(backingStoreFactory != null) {
            BackingStoreFactorySingleton.instance = backingStoreFactory;
        }
    }
    @Nullable
    public <ModelType extends Parsable> CompletableFuture<List<ModelType>> sendCollectionAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final ParsableFactory<ModelType> factory, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        Objects.requireNonNull(factory, "parameter factory cannot be null");

        final Span span = startSpan(requestInfo, "sendCollectionAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return CompletableFuture.completedFuture(null);
                        }
                        final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getCollectionOfObjectValues").startSpan();
                        try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            final List<ModelType> result = rootNode.getCollectionOfObjectValues(factory);
                            setResponseType(result, span);
                            return CompletableFuture.completedFuture(result);
                        } finally {
                            deserializationSpan.end();
                        }
                    } catch(ApiException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    private ResponseHandler getResponseHandler(final RequestInformation requestInfo) {
        final Collection<RequestOption> requestOptions = requestInfo.getRequestOptions();
        for(final RequestOption rOption : requestOptions) {
            if (rOption instanceof ResponseHandlerOption) {
                final ResponseHandlerOption option = (ResponseHandlerOption)rOption;
                return option.getResponseHandler();
            }
        }
        return null;
    }
    private final static Pattern queryParametersCleanupPattern = Pattern.compile("\\{\\?[^\\}]+}", Pattern.CASE_INSENSITIVE);
    private final char[] queryParametersToDecodeForTracing = {'-', '.', '~', '$'};
    private Span startSpan(@Nonnull final RequestInformation requestInfo, @Nonnull final String methodName) {
        final String decodedUriTemplate = ParametersNameDecodingHandler.decodeQueryParameters(requestInfo.urlTemplate, queryParametersToDecodeForTracing);
        final String cleanedUriTemplate = queryParametersCleanupPattern.matcher(decodedUriTemplate).replaceAll("");
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder(methodName + " - " + cleanedUriTemplate).startSpan();
        span.setAttribute("http.uri_template", decodedUriTemplate);
        return span;
    }
    /** The key used for the event when a custom response handler is invoked. */
    @Nonnull
    public static final String eventResponseHandlerInvokedKey = "com.microsoft.kiota.response_handler_invoked";
    @Nullable
    public <ModelType extends Parsable> CompletableFuture<ModelType> sendAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final ParsableFactory<ModelType> factory, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        Objects.requireNonNull(factory, "parameter factory cannot be null");

        final Span span = startSpan(requestInfo, "sendAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return CompletableFuture.completedFuture(null);
                        }
                        final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getObjectValue").setParent(Context.current().with(span)).startSpan();
                        try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            final ModelType result = rootNode.getObjectValue(factory);
                            setResponseType(result, span);
                            return CompletableFuture.completedFuture(result);
                        } finally {
                            deserializationSpan.end();
                        }
                    } catch(ApiException ex) {
                        span.recordException(ex);
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        span.recordException(ex);
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    private void setResponseType(final Object result, final Span span) {
        if (result != null) {
            span.setAttribute("com.microsoft.kiota.response.type", result.getClass().getName());
        }
    }
    private void closeResponse(boolean closeResponse, Response response) {
        if(closeResponse && response.code() != 204) {
            response.close();
        }
    }
    private String getMediaTypeAndSubType(final MediaType mediaType) {
        return mediaType.type() + "/" + mediaType.subtype();
    }
    @Nullable
    public <ModelType> CompletableFuture<ModelType> sendPrimitiveAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final Class<ModelType> targetClass, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        final Span span = startSpan(requestInfo, "sendPrimitiveAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        if(targetClass == Void.class) {
                            return CompletableFuture.completedFuture(null);
                        } else {
                            if(targetClass == InputStream.class) {
                                final ResponseBody body = response.body();
                                if(body == null) {
                                    closeResponse = false;
                                    return CompletableFuture.completedFuture(null);
                                }
                                final InputStream rawInputStream = body.byteStream();
                                return CompletableFuture.completedFuture((ModelType)rawInputStream);
                            }
                            final ParseNode rootNode = getRootParseNode(response, span, span);
                            if (rootNode == null) {
                                closeResponse = false;
                                return CompletableFuture.completedFuture(null);
                            }
                            final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("get"+targetClass.getName()+"Value").setParent(Context.current().with(span)).startSpan();
                            try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                                Object result;
                                if(targetClass == Boolean.class) {
                                    result = rootNode.getBooleanValue();
                                } else if(targetClass == Byte.class) {
                                    result = rootNode.getByteValue();
                                } else if(targetClass == String.class) {
                                    result = rootNode.getStringValue();
                                } else if(targetClass == Short.class) {
                                    result = rootNode.getShortValue();
                                } else if(targetClass == BigDecimal.class) {
                                    result = rootNode.getBigDecimalValue();
                                } else if(targetClass == Double.class) {
                                    result = rootNode.getDoubleValue();
                                } else if(targetClass == Integer.class) {
                                    result = rootNode.getIntegerValue();
                                } else if(targetClass == Float.class) {
                                    result = rootNode.getFloatValue();
                                } else if(targetClass == Long.class) {
                                    result = rootNode.getLongValue();
                                } else if(targetClass == UUID.class) {
                                    result = rootNode.getUUIDValue();
                                } else if(targetClass == OffsetDateTime.class) {
                                    result = rootNode.getOffsetDateTimeValue();
                                } else if(targetClass == LocalDate.class) {
                                    result = rootNode.getLocalDateValue();
                                } else if(targetClass == LocalTime.class) {
                                    result = rootNode.getLocalTimeValue();
                                } else if(targetClass == Period.class) {
                                    result = rootNode.getPeriodValue();
                                } else if(targetClass == byte[].class) {
                                    result = rootNode.getByteArrayValue();
                                } else {
                                    throw new RuntimeException("unexpected payload type " + targetClass.getName());
                                }
                                setResponseType(result, span);
                                return CompletableFuture.completedFuture((ModelType)result);
                            } finally {
                                deserializationSpan.end();
                            }
                        }
                    } catch(ApiException ex) {
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    @Nullable
    public <ModelType extends Enum<ModelType>> CompletableFuture<ModelType> sendEnumAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final Class<ModelType> targetClass, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        final Span span = startSpan(requestInfo, "sendEnumAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return CompletableFuture.completedFuture(null);
                        }
                        final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getEnumValue").setParent(Context.current().with(span)).startSpan();
                        try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            final Object result = rootNode.getEnumValue(targetClass);
                            setResponseType(result, span);
                            return CompletableFuture.completedFuture((ModelType)result);
                        } finally {
                            deserializationSpan.end();
                        }
                    } catch(ApiException ex) {
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        return new CompletableFuture<ModelType>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    @Nullable
    public <ModelType extends Enum<ModelType>> CompletableFuture<List<ModelType>> sendEnumCollectionAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final Class<ModelType> targetClass, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        final Span span = startSpan(requestInfo, "sendEnumCollectionAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return CompletableFuture.completedFuture(null);
                        }
                        final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getCollectionOfEnumValues").setParent(Context.current().with(span)).startSpan();
                        try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            final Object result = rootNode.getCollectionOfEnumValues(targetClass);
                            setResponseType(result, span);
                            return CompletableFuture.completedFuture((List<ModelType>)result);
                        } finally {
                            deserializationSpan.end();
                        }
                    } catch(ApiException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    @Nullable
    public <ModelType> CompletableFuture<List<ModelType>> sendPrimitiveCollectionAsync(@Nonnull final RequestInformation requestInfo, @Nonnull final Class<ModelType> targetClass, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");

        final Span span = startSpan(requestInfo, "sendPrimitiveCollectionAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.getHttpResponseMessage(requestInfo, span, span, null)
            .thenCompose(response -> {
                final ResponseHandler responseHandler = getResponseHandler(requestInfo);
                if(responseHandler == null) {
                    boolean closeResponse = true;
                    try {
                        this.throwIfFailedResponse(response, span, errorMappings);
                        if(this.shouldReturnNull(response)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return CompletableFuture.completedFuture(null);
                        }
                        final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getCollectionOfPrimitiveValues").setParent(Context.current().with(span)).startSpan();
                        try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            final List<ModelType> result = rootNode.getCollectionOfPrimitiveValues(targetClass);
                            setResponseType(result, span);
                            return CompletableFuture.completedFuture(result);
                        } finally {
                            deserializationSpan.end();
                        }
                    } catch(ApiException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(ex);
                        }};
                    } catch(IOException ex) {
                        return new CompletableFuture<List<ModelType>>(){{
                            this.completeExceptionally(new RuntimeException("failed to read the response body", ex));
                        }};
                    } finally {
                        closeResponse(closeResponse, response);
                    }
                } else {
                    span.addEvent(eventResponseHandlerInvokedKey);
                    return responseHandler.handleResponseAsync(response, errorMappings);
                }
            });
        } finally {
            span.end();
        }
    }
    private ParseNode getRootParseNode(final Response response, final Span parentSpan, final Span spanForAttributes) throws IOException {
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getRootParseNode").setParent(Context.current().with(parentSpan)).startSpan();
        try(final Scope scope = span.makeCurrent()) {
            final ResponseBody body = response.body(); // closing the response closes the body and stream https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/
            if(body == null) {
                return null;
            }
            final InputStream rawInputStream = body.byteStream();
            final ParseNode rootNode = pNodeFactory.getParseNode(getMediaTypeAndSubType(body.contentType()), rawInputStream);
            return rootNode;
        } finally {
            span.end();
        }
    }
    private boolean shouldReturnNull(final Response response) {
        final int statusCode = response.code();
        return statusCode == 204;
    }
    /** key used for the attribute when the error response has models mappings provided */
    @Nonnull
    public static final String errorMappingFoundAttributeName = "com.microsoft.kiota.error_mapping_found";
    /** Key used for the attribute when an error response body is found */
    @Nonnull
    public static final String errorBodyFoundAttributeName = "com.microsoft.kiota.error_body_found";
    private Response throwIfFailedResponse(@Nonnull final Response response, @Nonnull final Span spanForAttributes, @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) throws IOException, ApiException {
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("throwIfFailedResponse").setParent(Context.current().with(spanForAttributes)).startSpan();
        try(final Scope scope = span.makeCurrent()) {
            if (response.isSuccessful()) return response;
            spanForAttributes.setStatus(StatusCode.ERROR);

            final String statusCodeAsString = Integer.toString(response.code());
            final Integer statusCode = response.code();
            if (errorMappings == null ||
            !errorMappings.containsKey(statusCodeAsString) &&
            !(statusCode >= 400 && statusCode < 500 && errorMappings.containsKey("4XX")) &&
            !(statusCode >= 500 && statusCode < 600 && errorMappings.containsKey("5XX"))) {
		        spanForAttributes.setAttribute(errorMappingFoundAttributeName, false);
                final ApiException result = new ApiException("the server returned an unexpected status code and no error class is registered for this code " + statusCode);
                spanForAttributes.recordException(result);
                throw result;
            }
            spanForAttributes.setAttribute(errorMappingFoundAttributeName, true);

            final ParsableFactory<? extends Parsable> errorClass = errorMappings.containsKey(statusCodeAsString) ?
                                                        errorMappings.get(statusCodeAsString) :
                                                        (statusCode >= 400 && statusCode < 500 ?
                                                            errorMappings.get("4XX") :
                                                            errorMappings.get("5XX"));
            boolean closeResponse = true;
            try {
                final ParseNode rootNode = getRootParseNode(response, span, span);
                if(rootNode == null) {
		            spanForAttributes.setAttribute(errorBodyFoundAttributeName, false);
                    closeResponse = false;
                    final ApiException result = new ApiException("service returned status code" + statusCode + " but no response body was found");
                    spanForAttributes.recordException(result);
                    throw result;
                }
                spanForAttributes.setAttribute(errorBodyFoundAttributeName, true);
                final Span deserializationSpan = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getObjectValue").setParent(Context.current().with(span)).startSpan();
                try(final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                    final Parsable error = rootNode.getObjectValue(errorClass);
                    ApiException result;
                    if (error instanceof ApiException) {
                        result = (ApiException)error;
                    } else {
                        result = new ApiException("unexpected error type " + error.getClass().getName());
                    }
                    spanForAttributes.recordException(result);
                    throw result;
                } finally {
                    deserializationSpan.end();
                }
            } finally {
                closeResponse(closeResponse, response);
            }
        } finally {
            span.end();
        }
    }
    private final static String claimsKey = "claims";
    private CompletableFuture<Response> getHttpResponseMessage(@Nonnull final RequestInformation requestInfo, @Nonnull final Span parentSpan, @Nonnull final Span spanForAttributes, @Nullable final String claims) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getHttpResponseMessage").setParent(Context.current().with(parentSpan)).startSpan();
        try(final Scope scope = span.makeCurrent()) {
            this.setBaseUrlForRequestInformation(requestInfo);
            final Map<String, Object> additionalContext = new HashMap<String, Object>() {{
                put("parent-span", span);
            }};
            if(claims != null && !claims.isEmpty()) {
                additionalContext.put(claimsKey, claims);
            }
            return this.authProvider.authenticateRequest(requestInfo, additionalContext)
            .thenCompose(x -> {
                try {
                    final OkHttpCallbackFutureWrapper wrapper = new OkHttpCallbackFutureWrapper();
                    this.client.newCall(getRequestFromRequestInformation(requestInfo, span, spanForAttributes)).enqueue(wrapper);
                    return wrapper.future;
                } catch (URISyntaxException | MalformedURLException ex) {
                    spanForAttributes.recordException(ex);
                    final CompletableFuture<Response> result = new CompletableFuture<Response>();
                    result.completeExceptionally(ex);
                    return result;
                }
            })
            .thenApply(x -> {
                final String contentLengthHeaderValue = getHeaderValue(x, "Content-Length");
                if(contentLengthHeaderValue != null && !contentLengthHeaderValue.isEmpty()) {
                    final Integer contentLengthHeaderValueAsInt = Integer.parseInt(contentLengthHeaderValue);
                    spanForAttributes.setAttribute(SemanticAttributes.HTTP_RESPONSE_CONTENT_LENGTH, contentLengthHeaderValueAsInt);
                }
                final String contentTypeHeaderValue = getHeaderValue(x, "Content-Length");
                if(contentTypeHeaderValue != null && !contentTypeHeaderValue.isEmpty()) {
                    spanForAttributes.setAttribute("http.response_content_type", contentTypeHeaderValue);
                }
                spanForAttributes.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, x.code());
                spanForAttributes.setAttribute(SemanticAttributes.HTTP_FLAVOR, x.protocol().toString().toUpperCase(Locale.ROOT));
                return x;
            })
            .thenCompose(x -> this.retryCAEResponseIfRequired(x, requestInfo, span, spanForAttributes, claims));
        } finally {
            span.end();
        }
    }
    private String getHeaderValue(final Response response, String key) {
        final List<String> headerValue = response.headers().values(key);
        if(headerValue != null && headerValue.size() > 0) {
            final String firstEntryValue = headerValue.get(0);
            if(firstEntryValue != null && !firstEntryValue.isEmpty()) {
                return firstEntryValue;
            }
        }
        return null;
    }
    private final static Pattern bearerPattern = Pattern.compile("^Bearer\\s.*", Pattern.CASE_INSENSITIVE);
    private final static Pattern claimsPattern = Pattern.compile("\\s?claims=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    /** Key used for events when an authentication challenge is returned by the API */
    @Nonnull
    public static final String authenticateChallengedEventKey = "com.microsoft.kiota.authenticate_challenge_received";
    private CompletableFuture<Response> retryCAEResponseIfRequired(@Nonnull final Response response, @Nonnull final RequestInformation requestInfo, @Nonnull final Span parentSpan, @Nonnull final Span spanForAttributes, @Nullable final String claims) {
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("retryCAEResponseIfRequired").setParent(Context.current().with(parentSpan)).startSpan();
        try(final Scope scope = span.makeCurrent()) {
            final String responseClaims = this.getClaimsFromResponse(response, requestInfo, claims);
            if (responseClaims != null && !responseClaims.isEmpty()) {
                if(requestInfo.content != null && requestInfo.content.markSupported()) {
                    try {
                        requestInfo.content.reset();
                    } catch (IOException ex) {
                        spanForAttributes.recordException(ex);
                        return new CompletableFuture<Response>(){{
                            this.completeExceptionally(ex);
                        }};
                    }
                }
                closeResponse(true, response);
                span.addEvent(authenticateChallengedEventKey);
                spanForAttributes.setAttribute(SemanticAttributes.HTTP_RETRY_COUNT, 1);
                return this.getHttpResponseMessage(requestInfo, span, spanForAttributes, responseClaims);
            }

            return CompletableFuture.completedFuture(response);
        } finally {
            span.end();
        }
    }
    String getClaimsFromResponse(@Nonnull final Response response, @Nonnull final RequestInformation requestInfo, @Nullable final String claims) {
        if(response.code() == 401 &&
           (claims == null || claims.isEmpty()) && // we avoid infinite loops and retry only once
           (requestInfo.content == null || requestInfo.content.markSupported())) {
               final List<String> authenticateHeader = response.headers("WWW-Authenticate");
               if(authenticateHeader != null && !authenticateHeader.isEmpty()) {
                    String rawHeaderValue = null;
                    for(final String authenticateEntry: authenticateHeader) {
                        final Matcher matcher = bearerPattern.matcher(authenticateEntry);
                        if(matcher.matches()) {
                            rawHeaderValue = authenticateEntry.replaceFirst("^Bearer\\s", "");
                            break;
                        }
                    }
                    if (rawHeaderValue != null) {
                        final String[] parameters = rawHeaderValue.split(",");
                        for(final String parameter: parameters) {
                            final Matcher matcher = claimsPattern.matcher(parameter);
                            if(matcher.matches()) {
                                return matcher.group(1);
                            }
                        }
                    }
                }
            }
            return null;
    }
    private void setBaseUrlForRequestInformation(@Nonnull final RequestInformation requestInfo) {
        Objects.requireNonNull(requestInfo);
        requestInfo.pathParameters.put("baseurl", getBaseUrl());
    }
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> CompletableFuture<T> convertToNativeRequestAsync(@Nonnull final RequestInformation requestInfo) {
        Objects.requireNonNull(requestInfo, "parameter requestInfo cannot be null");
        final Span span = startSpan(requestInfo, "convertToNativeRequestAsync");
        try(final Scope scope = span.makeCurrent()) {
            return this.authProvider.authenticateRequest(requestInfo, null)
                .thenApply(x -> {
                    try {
                        return (T) getRequestFromRequestInformation(requestInfo, span, span);
                    } catch (MalformedURLException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
        } finally {
            span.end();
        }
    }
    private Request getRequestFromRequestInformation(@Nonnull final RequestInformation requestInfo, @Nonnull final Span parentSpan, @Nonnull final Span spanForAttributes) throws URISyntaxException, MalformedURLException {
        final Span span = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder("getRequestFromRequestInformation").setParent(Context.current().with(parentSpan)).startSpan();
        try(final Scope scope = span.makeCurrent()) {
            spanForAttributes.setAttribute(SemanticAttributes.HTTP_METHOD, requestInfo.httpMethod.toString());
            final URL requestURL = requestInfo.getUri().toURL();
            if (obsOptions.getIncludeEUIIAttributes()) {
                spanForAttributes.setAttribute(SemanticAttributes.HTTP_URL, requestURL.toString());
            }
            spanForAttributes.setAttribute("http.port", requestURL.getPort());
            spanForAttributes.setAttribute(SemanticAttributes.HTTP_HOST, requestURL.getHost());
            spanForAttributes.setAttribute(SemanticAttributes.HTTP_SCHEME, requestURL.getProtocol());

        
            final RequestBody body = requestInfo.content == null ? 
                null :
                new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        final Set<String> contentTypes = requestInfo.headers.containsKey(contentTypeHeaderKey) ? requestInfo.headers.get(contentTypeHeaderKey) : Set.of();
                        if(contentTypes.isEmpty()) {
                            return null;
                        } else {
                            final String contentType = contentTypes.toArray(new String[]{})[0];
                            spanForAttributes.setAttribute("http.request_content_type", contentType);
                            return MediaType.parse(contentType);
                        }
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        sink.writeAll(Okio.source(requestInfo.content));
                    }

                };
            final Request.Builder requestBuilder = new Request.Builder()
                                                .url(requestURL)
                                                .method(requestInfo.httpMethod.toString(), body);
            for (final Map.Entry<String,Set<String>> headerEntry : requestInfo.headers.entrySet()) {
                for(final String headerValue : headerEntry.getValue()) {
                    requestBuilder.addHeader(headerEntry.getKey(), headerValue);
                }
            }

            for(final RequestOption option : requestInfo.getRequestOptions()) {
                requestBuilder.tag(option.getType(), option);
            }
            if (requestBuilder.tag(obsOptions.getType()) == null) {
                requestBuilder.tag(obsOptions.getType(), obsOptions);
            }
            requestBuilder.tag(Span.class, parentSpan);
            final Request request = requestBuilder.build();
            final List<String> contentLengthHeader = request.headers().values("Content-Length");
            if(contentLengthHeader != null && contentLengthHeader.size() > 0) {
                final String firstEntryValue = contentLengthHeader.get(0);
                if(firstEntryValue != null && !firstEntryValue.isEmpty()) {
                    spanForAttributes.setAttribute(SemanticAttributes.HTTP_REQUEST_CONTENT_LENGTH, Long.parseLong(firstEntryValue));
                }
            }
            return request;
        } finally {
            span.end();
        }
    }
}
