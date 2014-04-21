package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * Represents a person who uses JIRA. This differs from a {@link User}, which represents a user in a directory.
 * An ApplicationUser encompasses all users with the same username (ignoring case) across all directories.
 *
 * This is intended to be used to allow for renaming of usernames in version 6.0.
 *
 * @since v5.1.1
 */
public interface ApplicationUser extends User
{
    /**
     * Returns the key which distinguishes the ApplicationUser as unique. The same key is shared by all users with the
     * same username (ignoring case) across all directories.
     *
     * @return the key which distinguishes the ApplicationUser as unique
     */
    String getKey();

    /**
     * Implementations must ensure equality based on getKey().
     *
     * @param obj object to compare to.
     * @return <code>true</code> if and only if the key matches.
     */
    boolean equals(Object obj);

    /**
     * Implementations must produce a hashcode based on getKey().
     *
     * @return hashcode.
     */
    int hashCode();
}
