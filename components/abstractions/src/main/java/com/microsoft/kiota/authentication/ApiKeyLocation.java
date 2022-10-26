package com.microsoft.kiota.authentication;

/**
 * The location of the API key in the request.
 */
public enum ApiKeyLocation {
	/** Add the API key as a query parameter */
	QUERY_PARAMETER,
	/** Add the API key as request header */
	HEADER
}