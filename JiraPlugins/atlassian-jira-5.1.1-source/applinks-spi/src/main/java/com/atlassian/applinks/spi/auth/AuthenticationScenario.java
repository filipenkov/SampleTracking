package com.atlassian.applinks.spi.auth;

/**
 * Describes the relationship between two Unified Application Links enabled servers, with respect to authentication
 *
 * @since 3.0
 */
public interface AuthenticationScenario
{

    /**
     * @return true if the two servers have (and will continue to have) the same collection of users.
     */
    boolean isCommonUserBase();

    /**
     * @return true if the two servers are secure and trust requests made to and from each other.
     */
    boolean isTrusted();

}
