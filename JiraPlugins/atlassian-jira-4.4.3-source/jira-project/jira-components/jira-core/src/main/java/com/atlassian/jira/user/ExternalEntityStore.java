/**
 * Copyright 2007 Atlassian Software.
 * All rights reserved.
 */

package com.atlassian.jira.user;

/**
 * This interface is to be implemented in order to provide ability to create user records in the external entities for
 * JIRA-Crowd integration.
 */
public interface ExternalEntityStore
{
    /**
     * Create a unique local entity that our ProfileProvider can use to get a profile if one does not already
     * exist for this name.
     *
     * @param name is a unique name that identifies a user, an id will be created and associated with this user.
     * @return Long id, this is the created or existing id for the name.
     */
    Long createIfDoesNotExist(String name);
}
