package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.internal.OnDemandDetector;
import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * Provides application name as alias, in the OnDemand mode only.
 *
 * @since 1.0
 */
public class ApplicationNameAliasProvider implements AdminLinkAliasProvider
{

    private final OnDemandDetector onDemandDetector;
    private final Set<String> appName;

    public ApplicationNameAliasProvider(OnDemandDetector onDemandDetector, ApplicationProperties applicationProperties)
    {
        this.onDemandDetector = onDemandDetector;
        this.appName = Collections.singleton(applicationProperties.getDisplayName());
    }

    @Override
    public Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext)
    {
        if (onDemandDetector.isOnDemandMode())
        {
            return appName;
        }
        else
        {
            return Collections.emptySet();
        }
    }
}
