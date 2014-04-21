package com.atlassian.jira.dashboard;

import java.net.URI;

/**
 * Helper interface to provide some utility methods for dealing with legacy portlet URIs.
 *
 * @since v4.0
 */
public interface LegacyGadgetUrlProvider
{
    static final String LEGACY_BRIDGET_GADGET_URI_PREFIX = "rest/gadget/1.0/legacy/spec/";
    static final String URI_EXTENSION = ".xml";
    static final String LEGACY_PORTLET_ID_PREF = "portlet_id";

    /**
     * Given the portletKey, this method concatenates this instances baseurl, prefix, key and extension to provide the
     * location of the gadget spec for this gadget.
     *
     * @param portletKey The portletKey for the legacy portlet
     * @return A URI containing the location of the gadgetSpec
     */
    URI getLegacyURI(String portletKey);

    /**
     * Given a URI this method checks if the URI is for a legacy portlet.
     *
     * @param gadgetUri The gadget spec URI
     * @return true if the uri is for a legacy portlet, false otherwise
     */
    boolean isLegacyGadget(URI gadgetUri);

    /**
     * Given a URI for legacy portlet spec, this method extracts the portlet key.
     *
     * @param gadgetUri The legacy portlet gadget spec URI
     * @return The legacy portletKey defined in the URI.
     * @throws IllegalArgumentException if the gadgetURI is not for a legacy portlet
     */
    String extractPortletKey(URI gadgetUri);
}
