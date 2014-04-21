package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkManager;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nonnull;
import java.util.Collections;

import static com.atlassian.administration.quicksearch.impl.spi.DefaultAdminWebItems.childLinks;
import static com.atlassian.administration.quicksearch.impl.spi.DefaultAdminWebItems.childSections;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link com.atlassian.administration.quicksearch.spi.AdminLinkManager}, based on
 * the Atlassian web-fragments API.
 *
 * @since 1.0
 */
public class DefaultAdminLinkManager implements AdminLinkManager
{
    private final WebInterfaceManager webInterfaceManager;

    public DefaultAdminLinkManager(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = checkNotNull(webInterfaceManager, "webInterfaceManager");
    }

    @Nonnull
    @Override
    public AdminLinkSection getSection(String location, UserContext userContext)
    {
        return rootSection(location, userContext);
    }

    protected AdminLinkSection rootSection(String location, UserContext userContext)
    {
        return new AdminLinkSectionBean(location, null, Collections.<String, String>emptyMap(), null,
                childSections(location, userContext, webInterfaceManager, linkFilter(userContext), sectionFilter(userContext)),
                childLinks(location, userContext, webInterfaceManager, linkFilter(userContext)));
    }

    protected Predicate<AdminLink> linkFilter(final UserContext userContext)
    {
        return Predicates.alwaysTrue();
    }

    protected Predicate<AdminLinkSection> sectionFilter(UserContext userContext)
    {
        return Predicates.alwaysTrue();
    }

}
