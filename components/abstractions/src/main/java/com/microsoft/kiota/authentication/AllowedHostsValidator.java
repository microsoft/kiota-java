package com.microsoft.kiota.authentication;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

/** Maintains a list of valid hosts and allows authentication providers to check whether a host is valid before authenticating a request */
public class AllowedHostsValidator {
    private HashSet<String> validHosts;
    /**
     * Creates a new AllowedHostsValidator.
     * @param validHosts The list of valid hosts.
     */
    public AllowedHostsValidator(@Nonnull final String... allowedHosts) {
        final HashSet<String> value = new HashSet<>(allowedHosts.length);
        for(final String val : allowedHosts) {
            value.add(val);
        }
        this.setAllowedHosts(value);
    }
    
    /**
     * Gets the allowed hosts. Read-only.
     * @return the allowed hosts.
     */
    @Nonnull
    public Set<String> getAllowedHosts() {
        return Collections.unmodifiableSet(this.validHosts);
    }
    /**
     * Sets the allowed hosts.
     * @param allowedHosts the allowed hosts.
     */
    public void setAllowedHosts(@Nonnull final Set<String> allowedHosts) {
        validHosts = new HashSet<String>();
        if(allowedHosts != null) {
            for (final String host : allowedHosts) {
                if (host != null && !host.isEmpty())
                    validHosts.add(host.trim().toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Checks if the provided host is allowed.
     * @param uri the uri to check the host for.
     * @return true if the host is allowed, false otherwise.
     */
    public boolean isUrlHostValid(@Nonnull final URI uri) {
        return validHosts.isEmpty() || validHosts.contains(uri.getHost().trim().toLowerCase(Locale.ROOT));
    }
}
