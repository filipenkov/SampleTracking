package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;

/**
 * Base class for events emitted when an {@link EntityLink} is modified
 *
 * @since 3.0
 */
public abstract class EntityLinkEvent implements LinkEvent
{
    protected final EntityLink entityLink;
    protected final String localKey;
    protected final Class<? extends EntityType> localType;

    protected EntityLinkEvent(final EntityLink entityLink, String localKey,  final Class<? extends EntityType> localType)
    {
        this.entityLink = entityLink;
        this.localKey = localKey;
        this.localType = localType;
    }

    /**
     * @return the globally unique, immutable ID of the server at the other
     *         end of this link.
     */
    public ApplicationId getApplicationId()
    {
        return entityLink.getApplicationLink().getId();
    }

    /**
     * @return the type of the application e.g. "fecru"
     */
    public ApplicationType getApplicationType()
    {
        return entityLink.getApplicationLink().getType();
    }

    /*
     * @return the type of the entity
     */
    public EntityType getEntityType()
    {
        return entityLink.getType();
    }

    /**
     * @return the remote project-level entity key (e.g. JRA, JIRAEXT)
     */
    public String getEntityKey()
    {
        return entityLink.getKey();
    }

    /**
     * @return the local key of the entity that this entity link belongs to.
     *
     * @since 3.2
     */
    public String getLocalKey()
    {
        return localKey;
    }

    /**
     * @return the type of the local entity that this entity link belongs to.
     *
     * @since 3.2
     */
    public Class<? extends EntityType> getLocalType()
    {
        return localType;
    }
}
