package com.microsoft.kiota.http.middleware.options;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.microsoft.kiota.RequestOption;

/** The options for the user agent handler */
public class UserAgentHandlerOption implements RequestOption {
	/** Creates a new instance of the user agent handler option */
	public UserAgentHandlerOption() { }
	private boolean enabled = true;
	@Nonnull
	private String productName = "kiota-java";
	@Nonnull
	private String productVersion = "0.2.0";
	/**
	 * Gets the product name to be used in the user agent header
	 * @return the product name
	 */
	@Nonnull
	public String getProductName() {
		return productName;
	}
	/**
	 * Sets the product name to be used in the user agent header
	 * @param value the product name
	 */
	public void setProductName(@Nonnull final String value) {
		Objects.requireNonNull(value, "parameter value cannot be null");
		productName = value;
	}
	/**
	 * Gets the product version to be used in the user agent header
	 * @return the product version
	 */
	@Nonnull
	public String getProductVersion() {
		return productVersion;
	}
	/**
	 * Sets the product version to be used in the user agent header
	 * @param value the product version
	 */
	public void setProductVersion(@Nonnull final String value) {
		Objects.requireNonNull(value, "parameter value cannot be null");
		productVersion = value;
	}
	/**
	 * Gets whether the user agent handler is enabled
	 * @return whether the user agent handler is enabled
	 */
	public boolean getEnabled() {
		return enabled;
	}
	/**
	 * Sets whether the user agent handler is enabled
	 * @param value whether the user agent handler is enabled
	 */
	public void setEnabled(final boolean value) {
		enabled = value;
	}
	/* @inheritdoc */
	@Override
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) UserAgentHandlerOption.class; 
	}
}
