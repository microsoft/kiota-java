package com.microsoft.kiota.http.middleware.options;

import javax.annotation.Nonnull;

import com.microsoft.kiota.RequestOption;
/** The ParametersEncodingOption request class */
public class ParametersNameDecodingOption implements RequestOption {
    /** Whether to decode the specified characters in the request query parameters names */
    public boolean enable = true;
    /** The list of characters to decode in the request query parameters names before executing the request */
    public char[] parametersToDecode = {'-', '.', '~', '$'};
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) ParametersNameDecodingOption.class; 
    }
}
