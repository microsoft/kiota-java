package com.microsoft.kiota.http;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

/**
 * This class contains the telemetry attribute keys used by this library.
 */
public final class TelemetrySemanticConventions {
    private TelemetrySemanticConventions() {}

    // https://opentelemetry.io/docs/specs/semconv/attributes-registry/

    /**
     * HTTP Response status code
     */
    public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
            longKey("http.response.status_code"); // stable

    /**
     * HTTP Request resend count
     */
    public static final AttributeKey<Long> HTTP_REQUEST_RESEND_COUNT =
            longKey("http.request.resend_count"); // stable

    /**
     * HTTP Request resend delay
     */
    public static final AttributeKey<Long> HTTP_REQUEST_RESEND_DELAY =
            longKey("http.request.resend_delay"); // stable

    /**
     * HTTP Request method
     */
    public static final AttributeKey<String> HTTP_REQUEST_METHOD =
            stringKey("http.request.method"); // stable

    /**
     * Network connection protocol version
     */
    public static final AttributeKey<String> NETWORK_PROTOCOL_NAME =
            stringKey("network.protocol.name"); // stable

    /**
     * Full HTTP request URL
     */
    public static final AttributeKey<String> URL_FULL = stringKey("url.full"); // stable

    /**
     * Full HTTP request URL template
     */
    public static final AttributeKey<String> URL_TEMPLATE = stringKey("url.uri_template"); // custom

    /**
     * HTTP request URL scheme
     */
    public static final AttributeKey<String> URL_SCHEME = stringKey("url.scheme"); // stable

    /**
     * HTTP request destination server address
     */
    public static final AttributeKey<String> SERVER_ADDRESS = stringKey("server.address"); // stable

    /**
     * HTTP request destination server port
     */
    public static final AttributeKey<Long> SERVER_PORT = longKey("server.port"); // stable

    /**
     * HTTP response body size
     */
    public static final AttributeKey<Long> EXPERIMENTAL_HTTP_RESPONSE_BODY_SIZE =
            longKey("http.response.body.size"); // experimental

    /**
     * HTTP request body size
     */
    public static final AttributeKey<Long> EXPERIMENTAL_HTTP_REQUEST_BODY_SIZE =
            longKey("http.request.body.size"); // experimental

    /**
     * HTTP response content type
     */
    public static final AttributeKey<String> HTTP_RESPONSE_HEADER_CONTENT_TYPE =
            stringKey("http.response.header.content-type"); // stable

    /**
     * HTTP request content type
     */
    public static final AttributeKey<String> HTTP_REQUEST_HEADER_CONTENT_TYPE =
            stringKey("http.request.header.content-type"); // stable
}
