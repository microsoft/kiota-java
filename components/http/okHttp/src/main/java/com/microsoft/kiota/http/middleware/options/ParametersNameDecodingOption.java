package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestOption;

import jakarta.annotation.Nonnull;

/** The ParametersEncodingOption request class */
public class ParametersNameDecodingOption implements RequestOption {
    /** Creates a new instance of the ParametersEncodingOption request class */
    public ParametersNameDecodingOption() {}

    /** Whether to decode the specified characters in the request query parameters names */
    public boolean enable = true;

    /** The list of characters to decode in the request query parameters names before executing the request */
    @Nonnull public char[] parametersToDecode = {'-', '.', '~', '$'};

    /* @inheritdoc */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) ParametersNameDecodingOption.class;
    }
}
