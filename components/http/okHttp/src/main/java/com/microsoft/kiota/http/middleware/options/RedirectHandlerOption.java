package com.microsoft.kiota.http.middleware.options;

import com.microsoft.kiota.RequestOption;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Options to be passed to the redirect middleware.
 */
public class RedirectHandlerOption implements RequestOption {
    private int maxRedirects;

    /**
     * The default maximum number of redirects to follow
     */
    public static final int DEFAULT_MAX_REDIRECTS = 5;

    /**
     * The absolute maximum number of redirects that can be followed
     */
    public static final int MAX_REDIRECTS = 20;

    @Nonnull private final IShouldRedirect shouldRedirect;

    /**
     * Default redirect evaluation, always follow redirect information.
     */
    @Nonnull public static final IShouldRedirect DEFAULT_SHOULD_REDIRECT = response -> true;

    /**
     * Create default instance of redirect options, with default values of max redirects and should redirect
     */
    public RedirectHandlerOption() {
        this(DEFAULT_MAX_REDIRECTS, DEFAULT_SHOULD_REDIRECT);
    }

    /**
     * Create an instance with provided values
     * @param maxRedirects Max redirects to occur
     * @param shouldRedirect Should redirect callback called before every redirect
     */
    public RedirectHandlerOption(int maxRedirects, @Nullable final IShouldRedirect shouldRedirect) {
        if (maxRedirects < 0)
            throw new IllegalArgumentException("Max redirects cannot be negative");
        if (maxRedirects > MAX_REDIRECTS)
            throw new IllegalArgumentException("Max redirect cannot exceed " + MAX_REDIRECTS);

        this.maxRedirects = maxRedirects;
        this.shouldRedirect = shouldRedirect != null ? shouldRedirect : DEFAULT_SHOULD_REDIRECT;
    }

    /**
     * Gets the maximum number of redirects to follow.
     * @return max redirects
     */
    public int maxRedirects() {
        return this.maxRedirects;
    }

    /**
     * Gets the callback evaluating whether a redirect should be followed.
     * @return should redirect
     */
    @Nonnull public IShouldRedirect shouldRedirect() {
        return this.shouldRedirect;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull public <T extends RequestOption> Class<T> getType() {
        return (Class<T>) RedirectHandlerOption.class;
    }
}
