package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;

/**
 * A middleware to replace the url with the specified replacement pairs.
 */
public class UrlReplaceHandler implements Interceptor {

    private UrlReplaceHandlerOption mUrlReplaceHandlerOption;

    /**
     * Instantiate a UrlReplaceHandler with default UrlReplaceHandlerOption.
     */
    public UrlReplaceHandler() {
        this(new UrlReplaceHandlerOption());
    }

    /**
     * Instantiate a UrlReplaceHandler with specified UrlReplaceHandlerOption
     * @param urlReplaceHandlerOption the specified UrlReplaceHandlerOption for the UrlReplaceHandler.
     */
    public UrlReplaceHandler(@Nonnull UrlReplaceHandlerOption urlReplaceHandlerOption) {
        Objects.requireNonNull(urlReplaceHandlerOption);
        this.mUrlReplaceHandlerOption =
                new UrlReplaceHandlerOption(
                        urlReplaceHandlerOption.getReplacementPairs(),
                        urlReplaceHandlerOption.isEnabled());
    }

    /** {@inheritDoc} */
    @Nonnull @Override
    public Response intercept(@Nonnull Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        Request request = Objects.requireNonNull(chain.request(), "request cannot be null");
        UrlReplaceHandlerOption replaceOption = request.tag(UrlReplaceHandlerOption.class);
        replaceOption = replaceOption == null ? mUrlReplaceHandlerOption : replaceOption;
        if (!replaceOption.isEnabled() || replaceOption.getReplacementPairs().isEmpty()) {
            return chain.proceed(request);
        }

        final Span span =
                ObservabilityHelper.getSpanForRequest(request, "UrlReplaceHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.urlreplace.enable", true);
        }
        try {
            request = replaceRequestUrl(request, replaceOption.getReplacementPairs());
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
        return chain.proceed(request);
    }

    /**
     * Gets the UrlReplaceHandlerOption for the UrlReplaceHandler.
     * @return the UrlReplaceHandlerOption for the UrlReplaceHandler.
     */
    @Nonnull public UrlReplaceHandlerOption getUrlReplaceHandlerOption() {
        return new UrlReplaceHandlerOption(
                mUrlReplaceHandlerOption.getReplacementPairs(),
                mUrlReplaceHandlerOption.isEnabled());
    }

    /**
     * Sets the UrlReplaceHandlerOption for the UrlReplaceHandler.
     * @param urlReplaceHandlerOption the UrlReplaceHandlerOption to set.
     */
    public void setUrlReplaceHandlerOption(
            @Nonnull UrlReplaceHandlerOption urlReplaceHandlerOption) {
        this.mUrlReplaceHandlerOption =
                new UrlReplaceHandlerOption(
                        urlReplaceHandlerOption.getReplacementPairs(),
                        urlReplaceHandlerOption.isEnabled());
    }

    /**
     * Replaces the url of the request using the replacement pairs provided.
     * @param request the request to replace the url of.
     * @param replacementPairs the replacement pairs to use.
     * @return the request with the updated url.
     */
    @Nonnull public static Request replaceRequestUrl(
            @Nonnull Request request, @Nonnull Map<String, String> replacementPairs) {
        Request.Builder builder = request.newBuilder();
        try {
            // Decoding the url since Request.url is encoded by default.
            String replacedUrl =
                    URLDecoder.decode(
                            request.url().toString(),
                            "UTF-8"); // Using decode(String,String) method to maintain source
            // compatibility with Java 8
            for (Map.Entry<String, String> entry : replacementPairs.entrySet()) {
                replacedUrl = replacedUrl.replace(entry.getKey(), entry.getValue());
            }
            builder.url(replacedUrl);
            return builder.build();

        } catch (UnsupportedEncodingException e) {
            return request; // This should never happen since "UTF-8" is a supported encoding.
        }
    }
}
