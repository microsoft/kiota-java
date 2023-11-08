package com.microsoft.kiota;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.SerializationWriter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.GlobalOpenTelemetry;

import io.github.stduritemplate.StdUriTemplate;


/** This class represents an abstract HTTP request. */
public class RequestInformation {
    /** Creates a new instance of the request information class. */
    public RequestInformation() { //Default constructor
    }
    /**
     * Creates a new instance of the request information class.
     * @param method The HTTP method for the request.
     * @param urlTemplate The url template for the request.
     * @param pathParameters The path parameters for the request.
     */
    public RequestInformation(@Nonnull final HttpMethod method, @Nonnull final String urlTemplate, @Nonnull final Map<String, Object> pathParameters) {
        this.httpMethod = Objects.requireNonNull(method);
        this.urlTemplate = Objects.requireNonNull(urlTemplate);
        this.pathParameters = Objects.requireNonNull(pathParameters);
    }
    /**
     * Configures the request information based on the request configuration and the query parameters getter.
     * @param <T> The type of the request configuration.
     * @param requestConfiguration The request configuration to apply to the request information.
     * @param configurationFactory The factory to create the request configuration from.
     */
    public <T extends BaseRequestConfiguration> void configure(@Nullable final java.util.function.Consumer<T> requestConfiguration, @Nonnull final java.util.function.Supplier<T> configurationFactory) {
        configure(requestConfiguration, configurationFactory, null);
    }
    /**
     * Configures the request information based on the request configuration and the query parameters getter.
     * @param <T> The type of the request configuration.
     * @param requestConfiguration The request configuration to apply to the request information.
     * @param configurationFactory The factory to create the request configuration from.
     * @param queryParametersGetter The function to get the query parameters from the request configuration.
     */
    public <T extends BaseRequestConfiguration> void configure(@Nullable final java.util.function.Consumer<T> requestConfiguration, @Nonnull final java.util.function.Supplier<T> configurationFactory, @Nullable final java.util.function.Function<T, Object> queryParametersGetter) {
        Objects.requireNonNull(configurationFactory);
        if (requestConfiguration == null)  {
            return;
        }
        final T requestConfig = configurationFactory.get();
        requestConfiguration.accept(requestConfig);
        if (queryParametersGetter != null) {
            addQueryParameters(queryParametersGetter.apply(requestConfig));
        }
        headers.putAll(requestConfig.headers);
        addRequestOptions(requestConfig.options);
    }
    /** The url template for the current request */
    @Nullable
    public String urlTemplate;
    /** The path parameters for the current request */
    @Nullable
    public Map<String, Object> pathParameters = new HashMap<>();
    private URI uri;
    /** Gets the URI of the request. 
     * @throws URISyntaxException when the uri template is invalid.
     * @throws IllegalStateException when the baseurl template parameter is missing from the path parameters.
     * @return the URI of the request.
     */
    @Nullable
    public URI getUri() throws URISyntaxException,IllegalStateException{
        if(uri != null) {
            return uri;
        } else if(pathParameters.containsKey(RAW_URL_KEY) &&
            pathParameters.get(RAW_URL_KEY) instanceof String) {
            setUri(new URI((String)pathParameters.get(RAW_URL_KEY)));
            return uri;
        } else {
            Objects.requireNonNull(urlTemplate);
            Objects.requireNonNull(queryParameters);
            if(!pathParameters.containsKey("baseurl") && urlTemplate.toLowerCase(Locale.ROOT).contains("{+baseurl}"))
                throw new IllegalStateException("PathParameters must contain a value for \"baseurl\" for the url to be built.");

            Map<String, Object> params = new HashMap<>(pathParameters.size() + queryParameters.size());
            params.putAll(pathParameters);
            params.putAll(queryParameters);

            
            return new URI(StdUriTemplate.expand(urlTemplate, params));
        }
    }
    /** 
     * Sets the URI of the request.
     * @param uri the URI of the request.
     */
    public void setUri(@Nonnull final URI uri) {
        this.uri = Objects.requireNonNull(uri);
        if(queryParameters != null) {
            queryParameters.clear();
        }
        if(pathParameters != null) {
            pathParameters.clear();
        }
    }
    static final String RAW_URL_KEY = "request-raw-url";
    /** The HTTP method for the request */
    @Nullable
    public HttpMethod httpMethod;

    private HashMap<String, Object> queryParameters = new HashMap<>();
    /**
     * Adds query parameters to the request based on the object passed in and its fields.
     * @param parameters The object to add the query parameters from.
     */
    public void addQueryParameters(@Nullable final Object parameters) {
        if (parameters == null) return;
        final Field[] fields = parameters.getClass().getFields();
        for(final Field field : fields) {
            try {
                final Object value = field.get(parameters);
                String name = field.getName();
                if (field.isAnnotationPresent(QueryParameter.class)) {
                    final String annotationName = field.getAnnotation(QueryParameter.class).name();
                    if(annotationName != null && !annotationName.isEmpty()) {
                        name = annotationName;
                    }
                }
                if(value != null) {
                    if(value.getClass().isArray()) {
                        queryParameters.put(name, Arrays.asList((Object[])value));
                    } else if(!value.toString().isEmpty()){
                        queryParameters.put(name, value);
                    }
                }
            } catch (IllegalAccessException ex) {
                //TODO log
            }
        }
    }
    /**
     * Adds query parameters to the request.
     * @param name The name of the query parameter.
     * @param value The value to add the query parameters.
     */
    public void addQueryParameter(@Nonnull final String name, @Nullable final Object value) {
        Objects.requireNonNull(name);
        queryParameters.put(name, value);
    }
    /**
     * Removes a query parameter from the request.
     * @param name The name of the query parameter to remove.
     */
    public void removeQueryParameter(@Nonnull final String name) {
        Objects.requireNonNull(name);
        queryParameters.remove(name);
    }
    /**
     * Gets the query parameters for the request.
     * @return The query parameters for the request.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public Map<String, Object> getQueryParameters() {
        return (Map<String, Object>) queryParameters.clone();
    }
    /** The request headers */
    @Nonnull
    public final RequestHeaders headers = new RequestHeaders();
    /** The Request Body. */
    @Nullable
    public InputStream content;
    @Nonnull
    private final HashMap<String, RequestOption> requestOptions = new HashMap<>();
    /**
     * Gets the request options for this request. Options are unique by type. If an option of the same type is added twice, the last one wins.
     * @return the request options for this request.
     */
    @Nonnull
    public Collection<RequestOption> getRequestOptions() { return requestOptions.values(); }
    /**
     * Adds request options to this request.
     * @param options the request options to add.
     */
    public void addRequestOptions(@Nullable final Collection<RequestOption> options) { 
        if(options == null || options.isEmpty()) return;
        for(final RequestOption option : options) {
            requestOptions.put(option.getClass().getCanonicalName(), option);
        }
    }
    /**
     * Removes a request option from this request.
     * @param options the request option to remove.
     */
    public void removeRequestOptions(@Nullable final RequestOption... options) {
        if(options == null || options.length == 0) return;
        for(final RequestOption option : options) {
            requestOptions.remove(option.getClass().getCanonicalName());
        }
    }
    /**
     * Adds a response handler as a RequestOption.
     * @param responseHandler the response handler to add to the request. 
     */
    public void setResponseHandler(@Nonnull ResponseHandler responseHandler) {
        Objects.requireNonNull(responseHandler);
        ResponseHandlerOption handlerOption = new ResponseHandlerOption();
        handlerOption.setResponseHandler(responseHandler);
        addRequestOptions(new ArrayList<>(Arrays.asList(handlerOption)));
    }
    @Nonnull
    private static final String BINARY_CONTENT_TYPE = "application/octet-stream";
    @Nonnull
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    /**
     * Sets the request body to be a binary stream.
     * @param value the binary stream
     * @deprecated use {@link #setStreamContent(InputStream, String)} instead.
     */
    @Deprecated
    public void setStreamContent(@Nonnull final InputStream value) {
        setStreamContent(value, BINARY_CONTENT_TYPE);
    }
    /**
     * Sets the request body to be a binary stream.
     * @param value the binary stream
     * @param contentType the content type of the stream.
     */
    public void setStreamContent(@Nonnull final InputStream value, @Nonnull final String contentType) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(contentType);
        if (contentType.isEmpty()) {
            throw new IllegalArgumentException("contentType cannot be empty");
        }
        this.content = value;
        headers.tryAdd(CONTENT_TYPE_HEADER, contentType);
    }
    private static final String SERIALIZE_ERROR = "could not serialize payload";
    private static final String SPAN_NAME = "setContentFromParsable";
    private static final String OBSERVABILITY_TRACER_NAME = "com.microsoft.kiota";
    /**
     * Sets the request body from a model with the specified content type.
     * @param values the models.
     * @param contentType the content type.
     * @param requestAdapter The adapter service to get the serialization writer from.
     * @param <T> the model type.
     */
    public <T extends Parsable> void setContentFromParsable(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String contentType, @Nonnull final T[] values) {
        final Span span = GlobalOpenTelemetry.getTracer(OBSERVABILITY_TRACER_NAME).spanBuilder(SPAN_NAME).startSpan();
        try (final Scope scope = span.makeCurrent()) {
            try(final SerializationWriter writer = getSerializationWriter(requestAdapter, contentType, values)) {
                headers.tryAdd(CONTENT_TYPE_HEADER, contentType);
                if (values.length > 0) {
                    setRequestType(values[0], span);
                }
                writer.writeCollectionOfObjectValues(null, Arrays.asList(values));
                this.content = writer.getSerializedContent();
            } catch (IOException ex) {
                final RuntimeException result = new RuntimeException(SERIALIZE_ERROR, ex);
                span.recordException(result);
                throw result;
            }
        } finally {
            span.end();
        }
    }
    /**
     * Sets the request body from a model with the specified content type.
     * @param value the model.
     * @param contentType the content type.
     * @param requestAdapter The adapter service to get the serialization writer from.
     * @param <T> the model type.
     */
    public <T extends Parsable> void setContentFromParsable(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String contentType, @Nonnull final T value) {
        final Span span = GlobalOpenTelemetry.getTracer(OBSERVABILITY_TRACER_NAME).spanBuilder(SPAN_NAME).startSpan();
        try (final Scope scope = span.makeCurrent()) {
            try(final SerializationWriter writer = getSerializationWriter(requestAdapter, contentType, value)) {
                String effectiveContentType = contentType;
                if (value instanceof MultipartBody) {
                    final MultipartBody multipartBody = (MultipartBody)value;
                    effectiveContentType += "; boundary=" + multipartBody.getBoundary();
                    multipartBody.requestAdapter = requestAdapter;
                }
                headers.tryAdd(CONTENT_TYPE_HEADER, effectiveContentType);
                setRequestType(value, span);
                writer.writeObjectValue(null, value);
                this.content = writer.getSerializedContent();
            } catch (IOException ex) {
                final RuntimeException result = new RuntimeException(SERIALIZE_ERROR, ex);
                span.recordException(result);
                throw result;
            }
        } finally {
            span.end();
        }
    }
    private void setRequestType(final Object result, final Span span) {
        if (result == null) return;
        if (span == null) return;
        span.setAttribute("com.microsoft.kiota.request.type", result.getClass().getName());
    }
    private <T> SerializationWriter getSerializationWriter(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String contentType, @Nonnull final T value)
    {
        Objects.requireNonNull(requestAdapter);
        Objects.requireNonNull(value);
        Objects.requireNonNull(contentType);

        return requestAdapter.getSerializationWriterFactory().getSerializationWriter(contentType);
    }
    /**
     * Sets the request body from a scalar value with the specified content type.
     * @param value the scalar values to serialize.
     * @param contentType the content type.
     * @param requestAdapter The adapter service to get the serialization writer from.
     * @param <T> the model type.
     */
    public <T> void setContentFromScalar(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String contentType, @Nonnull final T value) {
        final Span span = GlobalOpenTelemetry.getTracer(OBSERVABILITY_TRACER_NAME).spanBuilder(SPAN_NAME).startSpan();
        try (final Scope scope = span.makeCurrent()) {
            try(final SerializationWriter writer = getSerializationWriter(requestAdapter, contentType, value)) {
                headers.tryAdd(CONTENT_TYPE_HEADER, contentType);
                setRequestType(value, span);
                final Class<?> valueClass = value.getClass();
                if(valueClass.equals(String.class))
                    writer.writeStringValue(null, (String)value);
                else if(valueClass.equals(Boolean.class))
                    writer.writeBooleanValue(null, (Boolean)value);
                else if(valueClass.equals(Byte.class))
                    writer.writeByteValue(null, (Byte)value);
                else if(valueClass.equals(Short.class))
                    writer.writeShortValue(null, (Short)value);
                else if(valueClass.equals(BigDecimal.class))
                    writer.writeBigDecimalValue(null, (BigDecimal)value);
                else if(valueClass.equals(Float.class))
                    writer.writeFloatValue(null, (Float)value);
                else if(valueClass.equals(Long.class))
                    writer.writeLongValue(null, (Long)value);
                else if(valueClass.equals(Integer.class))
                    writer.writeIntegerValue(null, (Integer)value);
                else if(valueClass.equals(UUID.class))
                    writer.writeUUIDValue(null, (UUID)value);
                else if(valueClass.equals(OffsetDateTime.class))
                    writer.writeOffsetDateTimeValue(null, (OffsetDateTime)value);
                else if(valueClass.equals(LocalDate.class))
                    writer.writeLocalDateValue(null, (LocalDate)value);
                else if(valueClass.equals(LocalTime.class))
                    writer.writeLocalTimeValue(null, (LocalTime)value);
                else if(valueClass.equals(PeriodAndDuration.class))
                    writer.writePeriodAndDurationValue(null, (PeriodAndDuration)value);
                else {
                    final RuntimeException result = new RuntimeException("unknown type to serialize " + valueClass.getName());
                    span.recordException(result);
                    throw result;
                }
                this.content = writer.getSerializedContent();
            } catch (IOException ex) {
                final RuntimeException result = new RuntimeException(SERIALIZE_ERROR, ex);
                span.recordException(result);
                throw result;
            }
        } finally {
            span.end();
        }
    }
    /**
     * Sets the request body from a scalar value with the specified content type.
     * @param values the scalar values to serialize.
     * @param contentType the content type.
     * @param requestAdapter The adapter service to get the serialization writer from.
     * @param <T> the model type.
     */
    public <T> void setContentFromScalarCollection(@Nonnull final RequestAdapter requestAdapter, @Nonnull final String contentType, @Nonnull final T[] values) {
        final Span span = GlobalOpenTelemetry.getTracer(OBSERVABILITY_TRACER_NAME).spanBuilder(SPAN_NAME).startSpan();
        try (final Scope scope = span.makeCurrent()) {
            try(final SerializationWriter writer = getSerializationWriter(requestAdapter, contentType, values)) {
                headers.tryAdd(CONTENT_TYPE_HEADER, contentType);
                if (values.length > 0)
                    setRequestType(values[0], span);
                writer.writeCollectionOfPrimitiveValues(null, Arrays.asList(values));
                this.content = writer.getSerializedContent();
            } catch (IOException ex) {
                final RuntimeException result = new RuntimeException(SERIALIZE_ERROR, ex);
                span.recordException(result);
                throw result;
            }
        } finally {
            span.end();
        }
    }
}
