package com.atlassian.applinks.api;

import java.net.URI;

/**
 * Represents a link to a remote entity (Project, Space, Repository, etc).
 *
 * @since 3.0
 */
public interface EntityLink extends PropertySet
{

    /**
     * @return the link to the application that houses this entity
     */
    ApplicationLink getApplicationLink();

    /**
     * @return the type of the entity
     */
    EntityType getType();

    /**
     * @return the remote project-level entity key (e.g. JRA, JIRAEXT)
     */
    String getKey();

    /**
     * @return the name of the remote entity (e.g. "My JIRA Project", "My FishEye Repository"), or the value of
     *         {@link #getKey()} if no name is configured
     */
    String getName();

    /**
     * @return the display URL for this entity (e.g. http://jira.atlassian.com/browse/JRA) or null if there is no
     *         displayable url.
     */
    URI getDisplayUrl();

    /**
     * @return true if this is the primary link of its type for the local entity context
     */
    boolean isPrimary();

}
