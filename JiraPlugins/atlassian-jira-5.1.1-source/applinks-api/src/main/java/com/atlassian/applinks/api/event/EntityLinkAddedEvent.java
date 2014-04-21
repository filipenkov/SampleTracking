package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;

/**
 * This event is broadcast after an entity link is added and the new primary optionally set.
 *
 * @since 3.0
 */
public class EntityLinkAddedEvent extends EntityLinkEvent
{

    public EntityLinkAddedEvent(final EntityLink entityLink, String localKey, final Class<? extends EntityType> localType)
    {
        super(entityLink, localKey, localType);
    }

    /**
     * @return the {@link EntityLink} that is the subject of the event
     */
    public EntityLink getEntityLink()
    {
        return entityLink;
    }

}