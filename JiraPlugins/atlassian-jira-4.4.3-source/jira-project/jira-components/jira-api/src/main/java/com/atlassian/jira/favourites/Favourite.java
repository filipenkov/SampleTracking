/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.favourites;

/**
 * An entity that can be marked as favourite by users.
 * 
 * @since v3.13
 */
public interface Favourite
{
    /**
     * The number of users who have marked this entity as one of their favourites.
     * 
     * @return long the user count
     */
    Long getFavouriteCount();
}
