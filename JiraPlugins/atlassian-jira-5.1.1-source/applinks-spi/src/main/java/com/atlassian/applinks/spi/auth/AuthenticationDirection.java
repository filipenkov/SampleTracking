package com.atlassian.applinks.spi.auth;

/**
 * Used to identify the direction of invocations between two application when
 * configuring authentication providers.
 *
 * @since   3.0
 */
public enum AuthenticationDirection
{
    /**
     * Represents invocations made <em>into</em> this application, affecting
     * inbound authentication configuration on this host.
     */
    INBOUND,

    /**
     * Represents invocations made <em>by</em> this application into another,
     * remote application, affecting outbound authentication configuration on
     * this host.
     */
    OUTBOUND
}
