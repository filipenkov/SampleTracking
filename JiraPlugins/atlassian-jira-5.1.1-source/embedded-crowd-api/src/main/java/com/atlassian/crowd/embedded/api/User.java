package com.atlassian.crowd.embedded.api;

import java.security.Principal;

/**
 * Represents a user.
 */
public interface User extends Comparable<User>, Principal
{
    /**
     * @return id of the directory in which the User is stored.
     */
    long getDirectoryId();

    /**
     * @return <code>true<code> if and only if the user is allowed to authenticate.
     */
    boolean isActive();

    /**
     * @return email address of the user.
     */
    String getEmailAddress();

    /**
     * @return display name (eg. full name) of the user, must never be null.
     */
    String getDisplayName();

    /**
     * Implementations must ensure equality based on
     * getDirectoryId() and case-insensitive getName().
     *
     * @param o object to compare to.
     * @return <code>true</code> if and only if the directoryId
     *         and name.toLowerCase() of the directory entities match.
     */
    boolean equals(Object o);

    /**
     * Implementations must produce a hashcode based on
     * getDirectoryId() and case-insensitive getName().
     *
     * @return hashcode.
     */
    int hashCode();

    /**
     * CompareTo must be compatible with the equals() and hashCode() methods
     */
    int compareTo(User user);
}
