package com.microsoft.kiota.http.middleware;

import com.microsoft.kiota.http.middleware.options.UserAgentHandlerOption;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/** Adds the current library version as a product to the user agent header */
public class UserAgentHandler implements Interceptor {
    @Nonnull private final UserAgentHandlerOption _userAgentOption;

    /**
     * Creates a new instance of the user agent handler with the default options
     */
    public UserAgentHandler() {
        this(null);
    }

    /**
     * Creates a new instance of the user agent handler with the provided options
     * @param userAgentHandlerOption the options to use
     */
    public UserAgentHandler(@Nullable UserAgentHandlerOption userAgentHandlerOption) {
        if (userAgentHandlerOption == null) {
            _userAgentOption = new UserAgentHandlerOption();
        } else {
            _userAgentOption = userAgentHandlerOption;
        }
    }

    private static final String USER_AGENT_HEADER_NAME = "User-Agent";

    /* @inheritdoc */
    @Override
    @SuppressWarnings("UnknownNullness")
    public Response intercept(final Chain chain) throws IOException {
        Objects.requireNonNull(chain, "parameter chain cannot be null");
        final Request request = chain.request();

        UserAgentHandlerOption userAgentHandlerOption = request.tag(UserAgentHandlerOption.class);
        if (userAgentHandlerOption == null) {
            userAgentHandlerOption = this._userAgentOption;
        }

        if (!userAgentHandlerOption.getEnabled()) return chain.proceed(request);

        final Span span =
                ObservabilityHelper.getSpanForRequest(request, "UserAgentHandler_Intercept");
        Scope scope = null;
        if (span != null) {
            scope = span.makeCurrent();
            span.setAttribute("com.microsoft.kiota.handler.useragent.enable", true);
        }
        final Request.Builder builder = request.newBuilder();
        try {
            String currentValue = request.headers().get(USER_AGENT_HEADER_NAME);
            final String valueToAppend =
                    userAgentHandlerOption.getProductName()
                            + "/"
                            + userAgentHandlerOption.getProductVersion();
            if (currentValue == null || currentValue.isEmpty()) {
                builder.header(USER_AGENT_HEADER_NAME, valueToAppend);
            } else if (!currentValue.contains(valueToAppend)) {
                builder.header(USER_AGENT_HEADER_NAME, currentValue + " " + valueToAppend);
            }
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.end();
            }
        }
        return chain.proceed(builder.build());
    }
}
