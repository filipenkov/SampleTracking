package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.GadgetSpecProvider;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletAccessManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.Transformed;

import java.net.URI;

import static com.atlassian.jira.util.Predicates.equalTo;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides all the legacy portlet specs for the gadgetbrowser in the new dashboard.
 *
 * @since v4.0
 */
public class JiraApplicationGadgetSpecProvider implements GadgetSpecProvider
{
    private final PortletAccessManager portletAccessManager;
    private final LegacyGadgetUrlProvider legacyGadgetUrlFactory;
    private static final String SYSTEM_PORTLET_PREFIX = "com.atlassian.jira.plugin.system.portlets";

    // get a porlet's URI
    private final Function<Portlet, URI> portletUriTransformer = new Function<Portlet, URI>()
    {
        public URI get(final Portlet portlet)
        {
            return legacyGadgetUrlFactory.getLegacyURI(portlet.getId());
        }
    };
    //only return non-system portlets.
    private final Predicate<Portlet>  nonSystemPortletsPredicate = new Predicate<Portlet>()
    {
        public boolean evaluate(final Portlet input)
        {
            return !input.getId().startsWith(SYSTEM_PORTLET_PREFIX);
        }
    };

    public JiraApplicationGadgetSpecProvider(final PortletAccessManager portletAccessManager, final LegacyGadgetUrlProvider legacyGadgetUrlFactory)
    {
        this.portletAccessManager = notNull("portletAccessManager", portletAccessManager);
        this.legacyGadgetUrlFactory = notNull("legacyGadgetUrlFactory", legacyGadgetUrlFactory);
    }

    public Iterable<URI> entries()
    {
        return Transformed.iterable(CollectionUtil.filter(portletAccessManager.getAllPortlets(), nonSystemPortletsPredicate), portletUriTransformer);
    }

    public boolean contains(final URI uri)
    {
        notNull("uri", uri);

        URI relativeURI = uri;
        final String urlString = relativeURI.toASCIIString();
        if(relativeURI.isAbsolute() && urlString.contains(LegacyGadgetUrlProvider.LEGACY_BRIDGET_GADGET_URI_PREFIX))
        {
            final String relativeString = urlString.substring(urlString.indexOf(LegacyGadgetUrlProvider.LEGACY_BRIDGET_GADGET_URI_PREFIX));
            relativeURI = URI.create(relativeString);
        }
        return CollectionUtil.contains(entries(), equalTo(relativeURI));
    }
}
