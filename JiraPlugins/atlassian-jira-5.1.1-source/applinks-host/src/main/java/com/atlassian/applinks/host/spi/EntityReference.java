package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.EntityType;

/**
 * Represents an application "project-level" entity (e.g. JIRA project, Confluence space, FishEye repository, etc.)
 *
 * @since 3.0
 */
public interface EntityReference
{

    /**
     * @return the String key of the entity, (e.g. JRA, CRUC, CR-FE, etc.)
     */
    String getKey();

    /**
     * @return the type of the entity (e.g. jira-project, fisheye-repository, etc.)
     */
    EntityType getType();

    /**
     * @return the name of the remote entity (e.g. "My JIRA Project", "My FishEye Repository")
     */
    String getName();
}