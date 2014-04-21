package com.atlassian.upm.token;

/**
 * Manages tokens meant to be used as XSRF protection.
 */
public interface TokenManager
{
    /**
     * Retrieves, if existing, or creates valid token for the specified user.
     *
     * @param username the user to get the token for
     * @return the valid token
     */
    public String getTokenForUser(String username);

    /**
     * Checks if the provided token matches the stored token for the user. Returns {@code true} if it matches and is not expired
     * and {@code false} otherwise. If the token matches, a new token is created and stored in its place.
     *
     * @param username the user whose token should be checked
     * @param tokenValue the token to check against the user's stored token
     * @return {@code true} if the token matches and is not expired, {@code false} otherwise
     */
    public boolean attemptToMatchAndInvalidateToken(String username, String tokenValue);
}
