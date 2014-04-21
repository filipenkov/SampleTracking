package com.atlassian.applinks.spi.link;

import com.atlassian.applinks.api.ApplicationLink;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * POJO for holding attributes that may be updated during an {@link ApplicationLink}'s life-cycle
 *
 * @see MutableApplicationLink#update(ApplicationLinkDetails)
 * @since 3.0
 */
public class ApplicationLinkDetails
{
    private final String name;
    private final URI displayUrl;
    private final URI rpcUrl;
    private final boolean isPrimary;

    /**
     * Use the {@link #builder()} method.
     */
    private ApplicationLinkDetails(final String name, final URI displayUrl, final URI rpcUrl, final boolean isPrimary)
    {
        this.name = name;
        this.displayUrl = displayUrl;
        this.rpcUrl = rpcUrl;
        this.isPrimary = isPrimary;
    }

    public String getName()
    {
        return name;
    }

    public URI getDisplayUrl()
    {
        return displayUrl;
    }

    public URI getRpcUrl()
    {
        return rpcUrl;
    }

    public boolean isPrimary()
    {
        return isPrimary;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * Creates a new {@link com.atlassian.applinks.spi.link.ApplicationLinkDetails.Builder}
     * initialized with the state from the supplied
     * {@link com.atlassian.applinks.spi.link.ApplicationLinkDetails}
     * instance.
     */
    public static Builder builder(final ApplicationLinkDetails details)
    {
        final Builder builder = new Builder();
        builder.displayUrl = details.displayUrl;
        builder.rpcUrl = details.rpcUrl;
        builder.name = details.name;
        builder.isPrimary = details.isPrimary;
        return builder;
    }

    /**
     * Creates a new {@link com.atlassian.applinks.spi.link.ApplicationLinkDetails.Builder}
     * initialized with the state from the supplied
     * {@link com.atlassian.applinks.api.ApplicationLink} instance.
     */
    public static Builder builder(final ApplicationLink applicationLink)
    {
        final Builder builder = new Builder();
        builder.displayUrl = applicationLink.getDisplayUrl();
        builder.rpcUrl = applicationLink.getRpcUrl();
        builder.name = applicationLink.getName();
        builder.isPrimary = applicationLink.isPrimary();
        return builder;
    }

    public static class Builder
    {
        private String name;
        private URI displayUrl;
        private URI rpcUrl;
        private boolean isPrimary;

        private Builder()
        {
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder displayUrl(final URI url)
        {
            this.displayUrl = url;
            return this;
        }

        public Builder rpcUrl(final URI url)
        {
            this.rpcUrl = url;
            return this;
        }

        public Builder isPrimary(final boolean isPrimary)
        {
            this.isPrimary = isPrimary;
            return this;
        }

        public ApplicationLinkDetails build()
        {
            // be nice - rpcUrl & displayUrl are generally the same thing, so accept either
            if (rpcUrl == null)
            {
                rpcUrl = displayUrl;
            }
            else if (displayUrl == null)
            {
                displayUrl = rpcUrl;
            }

            if (rpcUrl == null) {
                throw new NullPointerException("either displayUrl or rpcUrl must be set before build()");
            }

            return new ApplicationLinkDetails(checkNotNull(name, "name"), checkNotNull(displayUrl, "displayUrl"),
                    checkNotNull(rpcUrl, "rpcUrl"), isPrimary);
        }
    }

}
