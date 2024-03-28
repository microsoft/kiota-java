package com.microsoft.kiota.http;

import com.microsoft.kiota.*;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.http.middleware.ParametersNameDecodingHandler;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import com.microsoft.kiota.serialization.ParseNode;
import com.microsoft.kiota.serialization.ParseNodeFactory;
import com.microsoft.kiota.serialization.ParseNodeFactoryRegistry;
import com.microsoft.kiota.serialization.SerializationWriterFactory;
import com.microsoft.kiota.serialization.SerializationWriterFactoryRegistry;
import com.microsoft.kiota.serialization.ValuedEnumParser;
import com.microsoft.kiota.store.BackingStoreFactory;
import com.microsoft.kiota.store.BackingStoreFactorySingleton;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.NetworkAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import io.opentelemetry.semconv.incubating.HttpIncubatingAttributes;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.*;

import okio.BufferedSink;
import okio.Okio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** RequestAdapter implementation for OkHttp */
public class OkHttpRequestAdapter implements com.microsoft.kiota.RequestAdapter {
    private static final String contentTypeHeaderKey = "Content-Type";
    private static final String contentLengthHeaderKey = "Content-Length";
    @Nonnull private final Call.Factory client;
    @Nonnull private final AuthenticationProvider authProvider;
    @Nonnull private final ObservabilityOptions obsOptions;
    @Nonnull private ParseNodeFactory pNodeFactory;
    @Nonnull private SerializationWriterFactory sWriterFactory;
    @Nonnull private String baseUrl = "";

    public void setBaseUrl(@Nonnull final String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl);
    }

    @Nonnull public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     */
    public OkHttpRequestAdapter(@Nonnull final AuthenticationProvider authenticationProvider) {
        this(authenticationProvider, null, null, null, null);
    }

    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, and the parse node factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory) {
        this(authenticationProvider, parseNodeFactory, null, null, null);
    }

    /**
     * Instantiates a new OkHttp request adapter with the provided authentication provider, parse node factory, and the serialization writer factory.
     * @param authenticationProvider the authentication provider to use for authenticating requests.
     * @param parseNodeFactory the parse node factory to use for parsing responses.
     * @param serializationWriterFactory the serialization writer factory to use for serializing requests.
     */
    @SuppressWarnings("LambdaLast")
    public OkHttpRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory) {
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
    public OkHttpRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory,
            @Nullable final Call.Factory client) {
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
    public OkHttpRequestAdapter(
            @Nonnull final AuthenticationProvider authenticationProvider,
            @Nullable final ParseNodeFactory parseNodeFactory,
            @Nullable final SerializationWriterFactory serializationWriterFactory,
            @Nullable final Call.Factory client,
            @Nullable final ObservabilityOptions observabilityOptions) {
        this.authProvider =
                Objects.requireNonNull(
                        authenticationProvider, "parameter authenticationProvider cannot be null");
        if (client == null) {
            this.client = KiotaClientFactory.create().build();
        } else {
            this.client = client;
        }
        if (parseNodeFactory == null) {
            pNodeFactory = ParseNodeFactoryRegistry.defaultInstance;
        } else {
            pNodeFactory = parseNodeFactory;
        }

        if (serializationWriterFactory == null) {
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

    @Nonnull public SerializationWriterFactory getSerializationWriterFactory() {
        return sWriterFactory;
    }

    public void enableBackingStore(@Nullable final BackingStoreFactory backingStoreFactory) {
        this.pNodeFactory =
                Objects.requireNonNull(
                        ApiClientBuilder.enableBackingStoreForParseNodeFactory(pNodeFactory));
        this.sWriterFactory =
                Objects.requireNonNull(
                        ApiClientBuilder.enableBackingStoreForSerializationWriterFactory(
                                sWriterFactory));
        if (backingStoreFactory != null) {
            BackingStoreFactorySingleton.instance = backingStoreFactory;
        }
    }

    private static final String nullRequestInfoParameter = "parameter requestInfo cannot be null";
    private static final String nullEnumParserParameter = "parameter enumParser cannot be null";
    private static final String nullFactoryParameter = "parameter factory cannot be null";

    @Nullable public <ModelType extends Parsable> List<ModelType> sendCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ParsableFactory<ModelType> factory) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        Objects.requireNonNull(factory, nullFactoryParameter);

        final Span span = startSpan(requestInfo, "sendCollection");
        try (final Scope scope = span.makeCurrent()) {
            Response response = this.getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    final ParseNode rootNode = getRootParseNode(response, span, span);
                    if (rootNode == null) {
                        closeResponse = false;
                        return null;
                    }
                    final Span deserializationSpan =
                            GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                    .spanBuilder("getCollectionOfObjectValues")
                                    .startSpan();
                    try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                        final List<ModelType> result =
                                rootNode.getCollectionOfObjectValues(factory);
                        setResponseType(result, span);
                        return result;
                    } finally {
                        deserializationSpan.end();
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
        } finally {
            span.end();
        }
    }

    private ResponseHandler getResponseHandler(final RequestInformation requestInfo) {
        final Collection<RequestOption> requestOptions = requestInfo.getRequestOptions();
        for (final RequestOption rOption : requestOptions) {
            if (rOption instanceof ResponseHandlerOption) {
                final ResponseHandlerOption option = (ResponseHandlerOption) rOption;
                return option.getResponseHandler();
            }
        }
        return null;
    }

    private static final Pattern queryParametersCleanupPattern =
            Pattern.compile("\\{\\?[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
    private final char[] queryParametersToDecodeForTracing = {'-', '.', '~', '$'};

    private Span startSpan(
            @Nonnull final RequestInformation requestInfo, @Nonnull final String methodName) {
        final String decodedUriTemplate =
                ParametersNameDecodingHandler.decodeQueryParameters(
                        requestInfo.urlTemplate, queryParametersToDecodeForTracing);
        final String cleanedUriTemplate =
                queryParametersCleanupPattern.matcher(decodedUriTemplate).replaceAll("");
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder(methodName + " - " + cleanedUriTemplate)
                        .startSpan();
        span.setAttribute("http.uri_template", decodedUriTemplate);
        return span;
    }

    /** The key used for the event when a custom response handler is invoked. */
    @Nonnull public static final String eventResponseHandlerInvokedKey =
            "com.microsoft.kiota.response_handler_invoked";

    @Nullable public <ModelType extends Parsable> ModelType send(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ParsableFactory<ModelType> factory) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        Objects.requireNonNull(factory, nullFactoryParameter);

        final Span span = startSpan(requestInfo, "send");
        try (final Scope scope = span.makeCurrent()) {
            Response response = this.getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    final ParseNode rootNode = getRootParseNode(response, span, span);
                    if (rootNode == null) {
                        closeResponse = false;
                        return null;
                    }
                    final Span deserializationSpan =
                            GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                    .spanBuilder("getObjectValue")
                                    .setParent(Context.current().with(span))
                                    .startSpan();
                    try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                        final ModelType result = rootNode.getObjectValue(factory);
                        setResponseType(result, span);
                        return result;
                    } finally {
                        deserializationSpan.end();
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
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
        if (closeResponse && response.code() != 204) {
            response.close();
        }
    }

    @Nonnull private String getMediaTypeAndSubType(@Nonnull final MediaType mediaType) {
        return mediaType.type() + "/" + mediaType.subtype();
    }

    @Nullable public <ModelType> ModelType sendPrimitive(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final Class<ModelType> targetClass) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        Objects.requireNonNull(targetClass, "parameter targetClass cannot be null");
        final Span span = startSpan(requestInfo, "sendPrimitive");
        try (final Scope scope = span.makeCurrent()) {
            Response response = this.getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    if (targetClass == Void.class) {
                        return null;
                    } else {
                        if (targetClass == InputStream.class) {
                            closeResponse = false;
                            final ResponseBody body = response.body();
                            if (body == null) {
                                return null;
                            }
                            final InputStream rawInputStream = body.byteStream();
                            return (ModelType) rawInputStream;
                        }
                        final ParseNode rootNode = getRootParseNode(response, span, span);
                        if (rootNode == null) {
                            closeResponse = false;
                            return null;
                        }
                        final Span deserializationSpan =
                                GlobalOpenTelemetry.getTracer(
                                                obsOptions.getTracerInstrumentationName())
                                        .spanBuilder("get" + targetClass.getName() + "Value")
                                        .setParent(Context.current().with(span))
                                        .startSpan();
                        try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                            Object result;
                            if (targetClass == Boolean.class) {
                                result = rootNode.getBooleanValue();
                            } else if (targetClass == Byte.class) {
                                result = rootNode.getByteValue();
                            } else if (targetClass == String.class) {
                                result = rootNode.getStringValue();
                            } else if (targetClass == Short.class) {
                                result = rootNode.getShortValue();
                            } else if (targetClass == BigDecimal.class) {
                                result = rootNode.getBigDecimalValue();
                            } else if (targetClass == Double.class) {
                                result = rootNode.getDoubleValue();
                            } else if (targetClass == Integer.class) {
                                result = rootNode.getIntegerValue();
                            } else if (targetClass == Float.class) {
                                result = rootNode.getFloatValue();
                            } else if (targetClass == Long.class) {
                                result = rootNode.getLongValue();
                            } else if (targetClass == UUID.class) {
                                result = rootNode.getUUIDValue();
                            } else if (targetClass == OffsetDateTime.class) {
                                result = rootNode.getOffsetDateTimeValue();
                            } else if (targetClass == LocalDate.class) {
                                result = rootNode.getLocalDateValue();
                            } else if (targetClass == LocalTime.class) {
                                result = rootNode.getLocalTimeValue();
                            } else if (targetClass == PeriodAndDuration.class) {
                                result = rootNode.getPeriodAndDurationValue();
                            } else if (targetClass == byte[].class) {
                                result = rootNode.getByteArrayValue();
                            } else {
                                throw new RuntimeException(
                                        "unexpected payload type " + targetClass.getName());
                            }
                            setResponseType(result, span);
                            return (ModelType) result;
                        } finally {
                            deserializationSpan.end();
                        }
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
        } finally {
            span.end();
        }
    }

    @Nullable public <ModelType extends Enum<ModelType>> ModelType sendEnum(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ValuedEnumParser<ModelType> enumParser) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        Objects.requireNonNull(enumParser, nullEnumParserParameter);
        final Span span = startSpan(requestInfo, "sendEnum");
        try (final Scope scope = span.makeCurrent()) {
            Response response = this.getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    final ParseNode rootNode = getRootParseNode(response, span, span);
                    if (rootNode == null) {
                        closeResponse = false;
                        return null;
                    }
                    final Span deserializationSpan =
                            GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                    .spanBuilder("getEnumValue")
                                    .setParent(Context.current().with(span))
                                    .startSpan();
                    try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                        final Object result = rootNode.getEnumValue(enumParser::forValue);
                        setResponseType(result, span);
                        return (ModelType) result;
                    } finally {
                        deserializationSpan.end();
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
        } finally {
            span.end();
        }
    }

    @Nullable public <ModelType extends Enum<ModelType>> List<ModelType> sendEnumCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final ValuedEnumParser<ModelType> enumParser) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        Objects.requireNonNull(enumParser, nullEnumParserParameter);
        final Span span = startSpan(requestInfo, "sendEnumCollection");
        try (final Scope scope = span.makeCurrent()) {
            Response response = this.getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    final ParseNode rootNode = getRootParseNode(response, span, span);
                    if (rootNode == null) {
                        closeResponse = false;
                        return null;
                    }
                    final Span deserializationSpan =
                            GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                    .spanBuilder("getCollectionOfEnumValues")
                                    .setParent(Context.current().with(span))
                                    .startSpan();
                    try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                        final Object result =
                                rootNode.getCollectionOfEnumValues(enumParser::forValue);
                        setResponseType(result, span);
                        return (List<ModelType>) result;
                    } finally {
                        deserializationSpan.end();
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
        } finally {
            span.end();
        }
    }

    @Nullable public <ModelType> List<ModelType> sendPrimitiveCollection(
            @Nonnull final RequestInformation requestInfo,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings,
            @Nonnull final Class<ModelType> targetClass) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);

        final Span span = startSpan(requestInfo, "sendPrimitiveCollection");
        try (final Scope scope = span.makeCurrent()) {
            Response response = getHttpResponseMessage(requestInfo, span, span, null);
            final ResponseHandler responseHandler = getResponseHandler(requestInfo);
            if (responseHandler == null) {
                boolean closeResponse = true;
                try {
                    this.throwIfFailedResponse(response, span, errorMappings);
                    if (this.shouldReturnNull(response)) {
                        return null;
                    }
                    final ParseNode rootNode = getRootParseNode(response, span, span);
                    if (rootNode == null) {
                        closeResponse = false;
                        return null;
                    }
                    final Span deserializationSpan =
                            GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                    .spanBuilder("getCollectionOfPrimitiveValues")
                                    .setParent(Context.current().with(span))
                                    .startSpan();
                    try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                        final List<ModelType> result =
                                rootNode.getCollectionOfPrimitiveValues(targetClass);
                        setResponseType(result, span);
                        return result;
                    } finally {
                        deserializationSpan.end();
                    }
                } finally {
                    closeResponse(closeResponse, response);
                }
            } else {
                span.addEvent(eventResponseHandlerInvokedKey);
                return responseHandler.handleResponse(response, errorMappings);
            }
        } finally {
            span.end();
        }
    }

    @Nullable private ParseNode getRootParseNode(
            final Response response, final Span parentSpan, final Span spanForAttributes) {
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder("getRootParseNode")
                        .setParent(Context.current().with(parentSpan))
                        .startSpan();
        try (final Scope scope = span.makeCurrent()) {
            final ResponseBody body =
                    response.body(); // closing the response closes the body and stream
            // https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/
            if (body == null) {
                return null;
            }
            final InputStream rawInputStream = body.byteStream();
            final MediaType contentType = body.contentType();
            if (contentType == null) {
                return null;
            }
            return pNodeFactory.getParseNode(getMediaTypeAndSubType(contentType), rawInputStream);
        } finally {
            span.end();
        }
    }

    private boolean shouldReturnNull(final Response response) {
        final int statusCode = response.code();
        return statusCode == 204;
    }

    /** key used for the attribute when the error response has models mappings provided */
    @Nonnull public static final String errorMappingFoundAttributeName =
            "com.microsoft.kiota.error_mapping_found";

    /** Key used for the attribute when an error response body is found */
    @Nonnull public static final String errorBodyFoundAttributeName = "com.microsoft.kiota.error_body_found";

    private Response throwIfFailedResponse(
            @Nonnull final Response response,
            @Nonnull final Span spanForAttributes,
            @Nullable final HashMap<String, ParsableFactory<? extends Parsable>> errorMappings) {
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder("throwIfFailedResponse")
                        .setParent(Context.current().with(spanForAttributes))
                        .startSpan();
        try (final Scope scope = span.makeCurrent()) {
            if (response.isSuccessful()) return response;
            spanForAttributes.setStatus(StatusCode.ERROR);

            final String statusCodeAsString = Integer.toString(response.code());
            final int statusCode = response.code();
            final ResponseHeaders responseHeaders =
                    HeadersCompatibility.getResponseHeaders(response.headers());
            if (errorMappings == null
                    || !errorMappings.containsKey(statusCodeAsString)
                            && !(statusCode >= 400
                                    && statusCode < 500
                                    && errorMappings.containsKey("4XX"))
                            && !(statusCode >= 500
                                    && statusCode < 600
                                    && errorMappings.containsKey("5XX"))
                            && !errorMappings.containsKey("XXX")) {
                spanForAttributes.setAttribute(errorMappingFoundAttributeName, false);
                final ApiException result =
                        new ApiExceptionBuilder()
                                .withMessage(
                                        "the server returned an unexpected status code and no error"
                                                + " class is registered for this code "
                                                + statusCode)
                                .withResponseStatusCode(statusCode)
                                .withResponseHeaders(responseHeaders)
                                .build();
                spanForAttributes.recordException(result);
                throw result;
            }
            spanForAttributes.setAttribute(errorMappingFoundAttributeName, true);

            final ParsableFactory<? extends Parsable> errorClass =
                    errorMappings.containsKey(statusCodeAsString)
                            ? errorMappings.get(statusCodeAsString)
                            : (statusCode >= 400 && statusCode < 500
                                    ? errorMappings.getOrDefault("4XX", errorMappings.get("XXX"))
                                    : errorMappings.getOrDefault("5XX", errorMappings.get("XXX")));
            boolean closeResponse = true;
            try {
                final ParseNode rootNode = getRootParseNode(response, span, span);
                if (rootNode == null) {
                    spanForAttributes.setAttribute(errorBodyFoundAttributeName, false);
                    closeResponse = false;
                    final ApiException result =
                            new ApiExceptionBuilder()
                                    .withMessage(
                                            "service returned status code"
                                                    + statusCode
                                                    + " but no response body was found")
                                    .withResponseStatusCode(statusCode)
                                    .withResponseHeaders(responseHeaders)
                                    .build();
                    spanForAttributes.recordException(result);
                    throw result;
                }
                spanForAttributes.setAttribute(errorBodyFoundAttributeName, true);
                final Span deserializationSpan =
                        GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                                .spanBuilder("getObjectValue")
                                .setParent(Context.current().with(span))
                                .startSpan();
                try (final Scope deserializationScope = deserializationSpan.makeCurrent()) {
                    ApiException result =
                            new ApiExceptionBuilder(() -> rootNode.getObjectValue(errorClass))
                                    .withResponseStatusCode(statusCode)
                                    .withResponseHeaders(responseHeaders)
                                    .build();
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

    private static final String claimsKey = "claims";

    private Response getHttpResponseMessage(
            @Nonnull final RequestInformation requestInfo,
            @Nonnull final Span parentSpan,
            @Nonnull final Span spanForAttributes,
            @Nullable final String claims) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder("getHttpResponseMessage")
                        .setParent(Context.current().with(parentSpan))
                        .startSpan();
        try (final Scope scope = span.makeCurrent()) {
            this.setBaseUrlForRequestInformation(requestInfo);
            final Map<String, Object> additionalContext = new HashMap<String, Object>();
            additionalContext.put("parent-span", span);
            if (claims != null && !claims.isEmpty()) {
                additionalContext.put(claimsKey, claims);
            }
            this.authProvider.authenticateRequest(requestInfo, additionalContext);
            final Response response =
                    this.client
                            .newCall(
                                    getRequestFromRequestInformation(
                                            requestInfo, span, spanForAttributes))
                            .execute();
            final String contentLengthHeaderValue = getHeaderValue(response, "Content-Length");
            if (contentLengthHeaderValue != null && !contentLengthHeaderValue.isEmpty()) {
                final int contentLengthHeaderValueAsInt =
                        Integer.parseInt(contentLengthHeaderValue);
                spanForAttributes.setAttribute(
                        HttpIncubatingAttributes.HTTP_RESPONSE_BODY_SIZE,
                        contentLengthHeaderValueAsInt);
            }
            final String contentTypeHeaderValue = getHeaderValue(response, "Content-Length");
            if (contentTypeHeaderValue != null && !contentTypeHeaderValue.isEmpty()) {
                spanForAttributes.setAttribute(
                        "http.response_content_type", contentTypeHeaderValue);
            }
            spanForAttributes.setAttribute(
                    HttpAttributes.HTTP_RESPONSE_STATUS_CODE, response.code());
            spanForAttributes.setAttribute(
                    NetworkAttributes.NETWORK_PROTOCOL_VERSION,
                    response.protocol().toString().toUpperCase(Locale.ROOT));
            return this.retryCAEResponseIfRequired(
                    response, requestInfo, span, spanForAttributes, claims);
        } catch (IOException | URISyntaxException ex) {
            spanForAttributes.recordException(ex);
            throw new RuntimeException(ex);
        } finally {
            span.end();
        }
    }

    private String getHeaderValue(final Response response, String key) {
        final List<String> headerValue = response.headers().values(key);
        if (headerValue != null && headerValue.size() > 0) {
            final String firstEntryValue = headerValue.get(0);
            if (firstEntryValue != null && !firstEntryValue.isEmpty()) {
                return firstEntryValue;
            }
        }
        return null;
    }

    private static final Pattern bearerPattern =
            Pattern.compile("^Bearer\\s.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern claimsPattern =
            Pattern.compile("\\s?claims=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    /** Key used for events when an authentication challenge is returned by the API */
    @Nonnull public static final String authenticateChallengedEventKey =
            "com.microsoft.kiota.authenticate_challenge_received";

    private Response retryCAEResponseIfRequired(
            @Nonnull final Response response,
            @Nonnull final RequestInformation requestInfo,
            @Nonnull final Span parentSpan,
            @Nonnull final Span spanForAttributes,
            @Nullable final String claims) {
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder("retryCAEResponseIfRequired")
                        .setParent(Context.current().with(parentSpan))
                        .startSpan();
        try (final Scope scope = span.makeCurrent()) {
            final String responseClaims = this.getClaimsFromResponse(response, requestInfo, claims);
            if (responseClaims != null && !responseClaims.isEmpty()) {
                if (requestInfo.content != null && requestInfo.content.markSupported()) {
                    try {
                        requestInfo.content.reset();
                    } catch (IOException ex) {
                        spanForAttributes.recordException(ex);
                        throw new RuntimeException(ex);
                    }
                }
                closeResponse(true, response);
                span.addEvent(authenticateChallengedEventKey);
                spanForAttributes.setAttribute(HttpAttributes.HTTP_REQUEST_RESEND_COUNT, 1);
                return this.getHttpResponseMessage(
                        requestInfo, span, spanForAttributes, responseClaims);
            }
            return response;
        } finally {
            span.end();
        }
    }

    String getClaimsFromResponse(
            @Nonnull final Response response,
            @Nonnull final RequestInformation requestInfo,
            @Nullable final String claims) {
        if (response.code() == 401
                && (claims == null || claims.isEmpty())
                && // we avoid infinite loops and retry only once
                (requestInfo.content == null || requestInfo.content.markSupported())) {
            final List<String> authenticateHeader = response.headers("WWW-Authenticate");
            if (!authenticateHeader.isEmpty()) {
                String rawHeaderValue = null;
                for (final String authenticateEntry : authenticateHeader) {
                    final Matcher matcher = bearerPattern.matcher(authenticateEntry);
                    if (matcher.matches()) {
                        rawHeaderValue = authenticateEntry.replaceFirst("^Bearer\\s", "");
                        break;
                    }
                }
                if (rawHeaderValue != null) {
                    final String[] parameters = rawHeaderValue.split(",");
                    for (final String parameter : parameters) {
                        final Matcher matcher = claimsPattern.matcher(parameter);
                        if (matcher.matches()) {
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
    @Nonnull public <T> T convertToNativeRequest(@Nonnull final RequestInformation requestInfo) {
        Objects.requireNonNull(requestInfo, nullRequestInfoParameter);
        final Span span = startSpan(requestInfo, "convertToNativeRequest");
        try (final Scope scope = span.makeCurrent()) {
            this.authProvider.authenticateRequest(requestInfo, null);
            return (T) getRequestFromRequestInformation(requestInfo, span, span);
        } catch (URISyntaxException | MalformedURLException ex) {
            span.recordException(ex);
            throw new RuntimeException(ex);
        } finally {
            span.end();
        }
    }

    /**
     * Creates a new request from the request information instance.
     *
     * @param requestInfo       request information instance.
     * @param parentSpan        the parent span for telemetry.
     * @param spanForAttributes the span for the attributes.
     * @return the created request instance.
     * @throws URISyntaxException if the URI is invalid.
     * @throws MalformedURLException if the URL is invalid.
     */
    protected @Nonnull Request getRequestFromRequestInformation(
            @Nonnull final RequestInformation requestInfo,
            @Nonnull final Span parentSpan,
            @Nonnull final Span spanForAttributes)
            throws URISyntaxException, MalformedURLException {
        final Span span =
                GlobalOpenTelemetry.getTracer(obsOptions.getTracerInstrumentationName())
                        .spanBuilder("getRequestFromRequestInformation")
                        .setParent(Context.current().with(parentSpan))
                        .startSpan();
        try (final Scope scope = span.makeCurrent()) {
            spanForAttributes.setAttribute(
                    HttpAttributes.HTTP_REQUEST_METHOD, requestInfo.httpMethod.toString());
            final URL requestURL = requestInfo.getUri().toURL();
            if (obsOptions.getIncludeEUIIAttributes()) {
                spanForAttributes.setAttribute(UrlAttributes.URL_FULL, requestURL.toString());
            }
            spanForAttributes.setAttribute("http.port", requestURL.getPort());
            spanForAttributes.setAttribute(ServerAttributes.SERVER_ADDRESS, requestURL.getHost());
            spanForAttributes.setAttribute(UrlAttributes.URL_SCHEME, requestURL.getProtocol());

            RequestBody body =
                    requestInfo.content == null
                            ? null
                            : new RequestBody() {
                                @Override
                                public MediaType contentType() {
                                    final Set<String> contentTypes =
                                            requestInfo.headers.getOrDefault(
                                                    contentTypeHeaderKey, new HashSet<>());
                                    if (contentTypes.isEmpty()) {
                                        return null;
                                    } else {
                                        final String contentType =
                                                contentTypes.toArray(new String[] {})[0];
                                        spanForAttributes.setAttribute(
                                                "http.request_content_type", contentType);
                                        return MediaType.parse(contentType);
                                    }
                                }

                                @Override
                                public long contentLength() throws IOException {
                                    long length;
                                    final Set<String> contentLength =
                                            requestInfo.headers.getOrDefault(
                                                    contentLengthHeaderKey, new HashSet<>());
                                    if (contentLength.isEmpty()
                                            && requestInfo.content
                                                    instanceof ByteArrayInputStream) {
                                        final ByteArrayInputStream contentStream =
                                                (ByteArrayInputStream) requestInfo.content;
                                        length = contentStream.available();
                                    } else {
                                        length =
                                                Long.parseLong(
                                                        contentLength.toArray(new String[] {})[0]);
                                    }
                                    if (length <= 0) {
                                        length = super.contentLength();
                                    }
                                    if (length > 0) {
                                        spanForAttributes.setAttribute(
                                                HttpIncubatingAttributes.HTTP_REQUEST_BODY_SIZE,
                                                length);
                                    }
                                    return length;
                                }

                                @Override
                                public void writeTo(@Nonnull BufferedSink sink) throws IOException {
                                    sink.writeAll(Okio.source(requestInfo.content));
                                }
                            };

            // https://stackoverflow.com/a/35743536
            if (body == null
                    && (requestInfo.httpMethod.equals(HttpMethod.POST)
                            || requestInfo.httpMethod.equals(HttpMethod.PATCH)
                            || requestInfo.httpMethod.equals(HttpMethod.PUT))) {
                body = RequestBody.create(new byte[0]);
            }
            final Request.Builder requestBuilder =
                    new Request.Builder()
                            .url(requestURL)
                            .method(requestInfo.httpMethod.toString(), body);
            for (final Map.Entry<String, Set<String>> headerEntry :
                    requestInfo.headers.entrySet()) {
                for (final String headerValue : headerEntry.getValue()) {
                    requestBuilder.addHeader(headerEntry.getKey(), headerValue);
                }
            }
            boolean obsOptionsPresent = false;
            for (final RequestOption option : requestInfo.getRequestOptions()) {
                if (option.getType() == obsOptions.getType()) {
                    obsOptionsPresent = true;
                }
                requestBuilder.tag(option.getType(), option);
            }
            if (!obsOptionsPresent) {
                requestBuilder.tag(obsOptions.getType(), obsOptions);
            }
            requestBuilder.tag(Span.class, parentSpan);
            return requestBuilder.build();
        } finally {
            span.end();
        }
    }
}
