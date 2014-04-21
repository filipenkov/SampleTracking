package com.atlassian.gadgets.opensocial;

/**
 * An adapter for Shindig's SecurityToken interface. Host applications need not implement this interface. The AG
 * opensocial plugin will pass an implementation to the {@code PersonService}
 *
 *  @since 2.0
 */
public interface OpenSocialRequestContext
{

    /**
     * @return the owner of the request, or null if there is none.
     */
    public String getOwnerId();

    /**
     * @return the viewer of the request, or null if there is none.
     */
    public String getViewerId();

    /**
     * @return true if the request is for an anonymous viewer/owner
     */
    public boolean isAnonymous();

    /**
     * @return the URL being used by the current request
     *
     * The returned URL must contain at least protocol, host, and port.
     *
     * The returned URL may contain path or query parameters.
     *
     * @throws UnsupportedOperationException if the URL is not available.
     */
    public String getActiveUrl();

}
