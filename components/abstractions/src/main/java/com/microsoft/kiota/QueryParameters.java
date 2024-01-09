package com.microsoft.kiota;

import java.util.Map;

/** This interface allows to extract Query Parameters to be expanded in the URI Template. */
public interface QueryParameters {

    /**
     * Extracts the query parameters into a map for the URI template parsing.
     * @return a Map of String to Object
     */
    @jakarta.annotation.Nonnull Map<String, Object> toQueryParameters();
}
