package com.atlassian.jira.dashboard;

import com.atlassian.jira.util.dbc.Assertions;

import java.net.URI;

public class DefaultLegacyGadgetUrlProvider implements LegacyGadgetUrlProvider
{
    public URI getLegacyURI(final String portletKey)
    {
        Assertions.notNull("portletKey", portletKey);

        return URI.create(LEGACY_BRIDGET_GADGET_URI_PREFIX + portletKey + URI_EXTENSION);
    }

    public boolean isLegacyGadget(final URI gadgetUri)
    {
        Assertions.notNull("gadgetUri", gadgetUri);

        return gadgetUri.toASCIIString().contains(LEGACY_BRIDGET_GADGET_URI_PREFIX);
    }

    public String extractPortletKey(final URI gadgetUri)
    {
        Assertions.notNull("gadgetUri", gadgetUri);

        if(!isLegacyGadget(gadgetUri))
        {
            throw new IllegalArgumentException("gadgetUri '" + gadgetUri + "' is not a legacy gadget Uri!");
        }

        final String uri = gadgetUri.toASCIIString();
        final String uriEnd = uri.substring(uri.indexOf(LEGACY_BRIDGET_GADGET_URI_PREFIX) + LEGACY_BRIDGET_GADGET_URI_PREFIX.length());
        return uriEnd.substring(0, uriEnd.lastIndexOf(URI_EXTENSION));
    }
}
