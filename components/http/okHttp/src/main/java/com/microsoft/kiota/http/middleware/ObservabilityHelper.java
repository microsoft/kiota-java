package com.microsoft.kiota.http.middleware;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.http.ObservabilityOptions;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;

import okhttp3.Request;

class ObservabilityHelper {
	static Span getSpanForRequest(@Nonnull final Request request, @Nonnull final String spanName) {
		return getSpanForRequest(request, spanName, null);
	}
	static Span getSpanForRequest(@Nonnull final Request request, @Nonnull final String spanName, @Nullable final Span parentSpan) {
		Objects.requireNonNull(request, "parameter request cannot be null");
		Objects.requireNonNull(spanName, "parameter spanName cannot be null");
		final ObservabilityOptions obsOptions = request.tag(ObservabilityOptions.class);
        Span span = null;
        if (obsOptions != null) {
			Span parentToUse = parentSpan;
			if (parentToUse == null) {
            	parentToUse = request.tag(Span.class);
			}
            final SpanBuilder builder = GlobalOpenTelemetry.getTracer(obsOptions.GetTracerInstrumentationName()).spanBuilder(spanName);
            if (parentToUse != null) {
                builder.setParent(Context.current().with(parentToUse));
            }
            span = builder.startSpan();
        }
		return span;
	}
	
}
