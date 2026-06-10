package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestOption;

import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The options to be passed to the UrlReplaceHandler.
 * Defines the replacement pairs and whether the handler is enabled or not.
 */
public class UrlReplaceHandlerOption implements RequestOption {

    private Map<String, String> replacementPairs;
    private boolean enabled;

    /**
     * Instantiates a new UrlReplaceOption with an empty replacementPairs map and enabled set to true.
     */
    public UrlReplaceHandlerOption() {
        this(new HashMap<>());
    }

    /**
     * Instantiates a new UrlReplaceOption with the specified replacementPairs map and enabled set to true.
     * @param replacementPairs the replacement pairs map.
     */
    public UrlReplaceHandlerOption(@Nonnull Map<String, String> replacementPairs) {
        this(replacementPairs, true);
    }

    /**
     * Instantiates a new UrlReplaceOption with the specified replacementPairs map and enabled set to the specified value.
     * @param enabled whether the handler is enabled or not.
     * @param replacementPairs the replacement pairs map.
     */
    public UrlReplaceHandlerOption(@Nonnull Map<String, String> replacementPairs, boolean enabled) {
        Objects.requireNonNull(replacementPairs);
        this.replacementPairs = new HashMap<>(replacementPairs);
        this.enabled = enabled;
    }

    /**
     * Gets the replacement pairs map.
     * @return the replacement pairs map.
     */
    @Nonnull public Map<String, String> getReplacementPairs() {
        return new HashMap<>(replacementPairs);
    }

    /**
     * Sets the replacement pairs map.
     * @param replacementPairs the replacement pairs map.
     */
    public void setReplacementPairs(@Nonnull final Map<String, String> replacementPairs) {
        this.replacementPairs = new HashMap<>(replacementPairs);
    }

    /**
     * Enables the handler.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disables the handler.
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Gets whether the handler is enabled or not.
     * @return whether the handler is enabled or not.
     */
    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) UrlReplaceHandlerOption.class;
    }
}
