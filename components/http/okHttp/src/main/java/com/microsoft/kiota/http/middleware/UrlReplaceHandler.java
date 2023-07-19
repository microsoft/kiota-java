package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
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
     * Instantiate a GraphTelemetryHandler with default GraphClientOption.
     */
    public UrlReplaceHandler(){
        this(new UrlReplaceHandlerOption());
    }
    /**
     * Instantiate a GraphTelemetryHandler with specified GraphClientOption
     * @param urlReplaceHandlerOption the specified GraphClientOption for the GraphTelemetryHandler.
     */
    public UrlReplaceHandler(@Nonnull UrlReplaceHandlerOption urlReplaceHandlerOption){
        Objects.requireNonNull(urlReplaceHandlerOption);
        this.mUrlReplaceHandlerOption = new UrlReplaceHandlerOption(urlReplaceHandlerOption.getReplacementPairs(), urlReplaceHandlerOption.isEnabled());
    }
    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Response intercept(@Nonnull Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        Request request = Objects.requireNonNull(chain.request(), "request cannot be null");
        UrlReplaceHandlerOption replaceOption = request.tag(UrlReplaceHandlerOption.class);
        replaceOption = replaceOption == null ? mUrlReplaceHandlerOption : replaceOption;
        if(!replaceOption.isEnabled() || replaceOption.getReplacementPairs().isEmpty()) {
            return chain.proceed(request);
        }

        final Span span = ObservabilityHelper.getSpanForRequest(request, "UrlReplaceHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.urlreplace.enable", true);
        }
        try{
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
     * Gets the GraphClientOption for the GraphTelemetryHandler.
     * @return the GraphClientOption for the GraphTelemetryHandler.
     */
    public UrlReplaceHandlerOption getUrlReplaceHandlerOption() {
        return new UrlReplaceHandlerOption(mUrlReplaceHandlerOption.getReplacementPairs(), mUrlReplaceHandlerOption.isEnabled());
    }
    /**
     * Sets the GraphClientOption for the GraphTelemetryHandler.
     * @param urlReplaceHandlerOption the GraphClientOption to set.
     */
    public void setUrlReplaceHandlerOption(UrlReplaceHandlerOption urlReplaceHandlerOption) {
        this.mUrlReplaceHandlerOption = new UrlReplaceHandlerOption(urlReplaceHandlerOption.getReplacementPairs(), urlReplaceHandlerOption.isEnabled());
    }
    /**
     * Replaces the url of the request using the replacement pairs provided.
     * @param request the request to replace the url of.
     * @param replacementPairs the replacement pairs to use.
     * @return the request with the updated url.
     * @throws UnsupportedEncodingException if the url encoding is not supported.
     */
    @Nonnull
    public static Request replaceRequestUrl(@Nonnull Request request, @Nonnull Map<String, String> replacementPairs) throws UnsupportedEncodingException {
        Request.Builder builder = request.newBuilder();
        //Decoding the url since Request.url is encoded by default.
        String replacedUrl = URLDecoder.decode(request.url().toString(), "UTF-8");//Using decode(String,String) method to maintain source compatibility with Java 8
        for (Map.Entry<String, String> entry : replacementPairs.entrySet()) {
            replacedUrl = replacedUrl.replace(entry.getKey(), entry.getValue());
        }
        builder.url(replacedUrl);
        return builder.build();
    }
}
