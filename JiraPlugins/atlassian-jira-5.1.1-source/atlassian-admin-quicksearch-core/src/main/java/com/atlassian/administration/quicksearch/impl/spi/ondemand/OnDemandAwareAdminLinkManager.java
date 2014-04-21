package com.atlassian.administration.quicksearch.impl.spi.ondemand;

import com.atlassian.administration.quicksearch.impl.spi.AdminLinkSectionBean;
import com.atlassian.administration.quicksearch.impl.spi.DefaultAdminLinkManager;
import com.atlassian.administration.quicksearch.internal.OnDemandDetector;
import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * OnDemand-aware implementation of {@link com.atlassian.administration.quicksearch.spi.AdminLinkManager}.
 *
 */
public class OnDemandAwareAdminLinkManager extends DefaultAdminLinkManager
{
    private final OnDemandDetector onDemandDetector;

    public OnDemandAwareAdminLinkManager(WebInterfaceManager webInterfaceManager, OnDemandDetector onDemandDetector)
    {
        super(webInterfaceManager);
        this.onDemandDetector = onDemandDetector;
    }

    @Nonnull
    protected Map<String,String> defaultSections()
    {
        return Collections.emptyMap();
    }

    @Nonnull
    @Override
    public final AdminLinkSection getSection(String location, UserContext userContext)
    {
        final String defaultSectionLabel = defaultSections().get(location);
        if (onDemandDefaultSection(defaultSectionLabel))
        {
            // apply the label and wrap in a section that will get stripped
            final AdminLinkSection original = super.getSection(location, userContext);
            final AdminLinkSection withLabel = new AdminLinkSectionBean(original.getId(), defaultSectionLabel,
                    original.getParameters(), original.getLocation(), original.getSections(), original.getLinks());
            return new AdminLinkSectionBean(null, null, null, location, ImmutableList.of(withLabel), Collections.<AdminLink>emptyList());
        }
        else
        {
            return super.getSection(location, userContext);
        }
    }

    private boolean onDemandDefaultSection(String defaultSectionLabel)
    {
        return defaultSectionLabel != null && onDemandDetector.isOnDemandMode();
    }
}
