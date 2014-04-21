package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dark feature info.
 *
 * @since v5.1
 */
@PublicApi
@Immutable
public class FeatureEvent
{
    /**
     * A string containing the name of the dark feature.
     */
    @Nonnull
    private final String feature;

    /**
     * The name of the user for whom the dark feature was enabled. Null if it's a site-wide dark feature.
     */
    @Nullable
    private final String username;

    /**
     * Creates a new feature info.
     *
     * @param feature a String containing a feature name
     */
    protected FeatureEvent(@Nonnull String feature)
    {
        this(feature, null);
    }

    /**
     * Creates a new feature info for a per-user feature.
     *
     * @param feature a String containing a feature name
     * @param user a User (may be null)
     */
    protected FeatureEvent(@Nonnull String feature, @Nullable User user)
    {
        this.feature = checkNotNull(feature, "feature");
        this.username = user != null ? user.getName() : null;
    }

    /**
     * @return a string containing the name of the dark feature.
     */
    public String feature()
    {
        return feature;
    }

    /**
     * @return the name of the user for whom the dark feature was enabled. Null if it's a site-wide dark feature.
     */
    @Nullable
    public String username()
    {
        return username;
    }
}
