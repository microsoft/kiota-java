package com.microsoft.kiota.http;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

public class TelemetrySemanticConventions {

    // https://opentelemetry.io/docs/specs/semconv/attributes-registry/
    public static final AttributeKey HTTP_RESPONSE_STATUS_CODE =
            longKey("http.response.status_code"); // stable
    public static final AttributeKey HTTP_REQUEST_RESEND_COUNT =
            longKey("http.request.resend_count"); // stable
    public static final AttributeKey HTTP_REQUEST_METHOD =
            stringKey("http.request.method"); // stable
    public static final AttributeKey NETWORK_PROTOCOL_VERSION =
            stringKey("network.protocol.version"); // stable
    public static final AttributeKey URL_FULL = stringKey("url.full"); // stable
    public static final AttributeKey URL_SCHEME = stringKey("url.scheme"); // stable
    public static final AttributeKey SERVER_ADDRESS = stringKey("server.address"); // stable
    public static final AttributeKey SERVER_PORT = stringKey("server.port"); // stable

    public static final AttributeKey EXPERIMENTAL_HTTP_RESPONSE_BODY_SIZE =
            longKey("http.response.body.size"); // experimental
    public static final AttributeKey EXPERIMENTAL_HTTP_REQUEST_BODY_SIZE =
            longKey("http.request.body.size"); // experimental
}
