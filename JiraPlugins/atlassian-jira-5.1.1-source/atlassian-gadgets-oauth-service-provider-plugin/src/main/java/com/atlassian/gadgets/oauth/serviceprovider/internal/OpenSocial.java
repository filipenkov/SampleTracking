package com.atlassian.gadgets.oauth.serviceprovider.internal;

/**
 * Useful constants for OpenSocial stuff.
 */
final class OpenSocial
{
    /**
     * Preferred parameter, as it is the gadget spec defined parameter, that might contain the gadget spec url when the
     * request token is being requested.  
     */
    static final String XOAUTH_APP_URL = "xoauth_app_url";

    /**
     * Another possible parameter that might contain the gadget spec url when the request token is being requested.  It
     * is not specified in the spec, but Shindig uses it anyways.
     */
    static final String OPENSOCIAL_APP_URL = "opensocial_app_url";
}
