package com.atlassian.applinks.spi.manifest;

/**
 * @since   3.0
 */
public enum ApplicationStatus
{
    /**
     * <p>
     * Indicating the peer application is running normally.
     * </p>
     * <p>
     * Note that applications that, by design, are not capable of serving RPC
     * requests must still return this state, or AppLinks will display warning
     * icons.
     * </p>
     */
    AVAILABLE,

    /**
     * Indicating the peer application is not running normally.
     */
    UNAVAILABLE
}
