package com.microsoft.kiota.http;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

public final class TelemetrySemanticConventions {
    private TelemetrySemanticConventions() {}

    // https://opentelemetry.io/docs/specs/semconv/attributes-registry/
    public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
            longKey("http.response.status_code"); // stable
    public static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT =
            longKey("http.request.resend_count"); // stable
    public static final AttributeKey<String> HTTP_REQUEST_METHOD =
            stringKey("http.request.method"); // stable
    public static final AttributeKey<String> NETWORK_PROTOCOL_VERSION =
            stringKey("network.protocol.version"); // stable
    public static final AttributeKey<String> URL_FULL = stringKey("url.full"); // stable
    public static final AttributeKey<String> URL_SCHEME = stringKey("url.scheme"); // stable
    public static final AttributeKey<String> SERVER_ADDRESS = stringKey("server.address"); // stable
    public static final AttributeKey<Long> SERVER_PORT = longKey("server.port"); // stable

    public static final AttributeKey<Long> EXPERIMENTAL_HTTP_RESPONSE_BODY_SIZE =
            longKey("http.response.body.size"); // experimental
    public static final AttributeKey<Long> EXPERIMENTAL_HTTP_REQUEST_BODY_SIZE =
            longKey("http.request.body.size"); // experimental

    public static final AttributeKey<String> CUSTOM_HTTP_RESPONSE_CONTENT_TYPE =
            stringKey("http.response_content_type"); // custom
    public static final AttributeKey<String> CUSTOM_HTTP_REQUEST_CONTENT_TYPE =
            stringKey("http.request_content_type"); // custom
}
