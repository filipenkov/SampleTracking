package com.atlassian.applinks.core.rest.model;

/**
 * <code>
 * if (peer is OFFLINE)
 * {
 *   display "server offline" and "relocate" icon. "relocate" starts Relocate Wizard
 * }
 * else if (ServerID has changed)
 * {
 *   if (peer UAL.version >= 3)
 *   {
 *     peer must be upgraded -> start full Upgrade Wizard
 *   }
 *   else
 *   {
 *     peer must have downgraded from UAL to non-UAL -> start ServerID Change Wizard
 *   }
 * }
 * </code>
 *
 * @since   3.0
 */
public enum ApplicationLinkState
{
    /**
     * The peer is available (online) and it's manifest values (Server ID and
     * AppLinks version) match what we have locally.
     */
    OK,

    /**
     * Indicates that a peer is currently not available. This state corresponds
     * with
     * {@link com.atlassian.applinks.spi.manifest.ApplicationStatus#UNAVAILABLE}.
     */
    OFFLINE,

    /**
     * Indicates that a peer's Server ID is different than the one we have
     * stored locally, as a result of having been upgraded to UAL (which means
     * it now publishes its own manifest with Server ID, whereas previously we
     * used a locally generated UUID).
     */
    UPGRADED_TO_UAL,

    /**
     * Indicates that a peer's Server ID has changed, while still not
     * publishing a manifest. Since the peer is still non-UAL (it's not offline
     * and doesn't have a manifest), this state triggers the simplified upgrade
     * wizard that changes the Server ID in our local storage.
     */
    UPGRADED
}
