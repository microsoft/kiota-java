package com.microsoft.kiota.http.middleware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.http.middleware.options.ParametersNameDecodingOption;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

/** This handlers decodes special characters in the request query parameters that had to be encoded due to RFC 6570 restrictions names before executing the request. */
public class ParametersNameDecodingHandler implements Interceptor {
    private final ParametersNameDecodingOption options;
    /** Creates a new instance of the handler with default options */
    public ParametersNameDecodingHandler() {
        this(new ParametersNameDecodingOption());
    }
    /**
     * Creates a new instance of the handler with the provided options
     * @param options the options to use
     */
    public ParametersNameDecodingHandler(@Nonnull final ParametersNameDecodingOption options) {
        super();
        this.options = Objects.requireNonNull(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
	@SuppressWarnings("UnknownNullness")
    public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain);
        final Request request = chain.request();
        ParametersNameDecodingOption nameOption = request.tag(ParametersNameDecodingOption.class);
        if(nameOption == null) { nameOption = this.options; }
        final HttpUrl originalUri = request.url();
        final Span span = ObservabilityHelper.getSpanForRequest(request, "ParametersNameDecodingHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.parameters_name_decoding.enable", nameOption.enable);
        }
        try {
            if(!originalUri.toString().contains("%") ||
                nameOption == null ||
                !nameOption.enable ||
                nameOption.parametersToDecode == null ||
                nameOption.parametersToDecode.length == 0) {
                    return chain.proceed(request);
                }
            String query = originalUri.query();
            if (query == null || query.isEmpty()) {
                return chain.proceed(request);
            }
            query = decodeQueryParameters(query, nameOption.parametersToDecode);
            
            final Request.Builder builder = request.newBuilder();
            if (span != null) {
                builder.tag(Span.class, span);
            }
            final HttpUrl newUrl = originalUri.newBuilder().query(query).build();
            return chain.proceed(builder.url(newUrl).build());
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
    }
    /**
     * INTERNAL Decodes the query parameters that are in the list of parameters to decode
     * @param original the original query string
     * @param charactersToDecode the list of characters to decode
     * @return the decoded query string
     */
    @Nonnull
    public static String decodeQueryParameters(@Nullable final String original, @Nonnull final char[] charactersToDecode) {
        Objects.requireNonNull(charactersToDecode);
        String decoded = original == null ? "" : new StringBuffer(original).toString();
        final ArrayList<SimpleEntry<String, String>> symbolsToReplace = new ArrayList<SimpleEntry<String, String>>(charactersToDecode.length);
        for (final char charToReplace : charactersToDecode) {
            symbolsToReplace.add(new SimpleEntry<String,String>("%" + String.format("%x", (int)charToReplace), String.valueOf(charToReplace)));
        }
        for (final Entry<String, String> symbolToReplace : symbolsToReplace) {
            decoded = decoded.replace(symbolToReplace.getKey(), symbolToReplace.getValue());
        }
        return decoded;
    }
    
}
