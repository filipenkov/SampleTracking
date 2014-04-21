package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;

/**
 * This event is broadcast after an entity link is deleted and the new primary optionally set.
 * When this event occurs all properties of this entity link have been deleted.
 *
 * @since 3.0
 */
public class EntityLinkDeletedEvent extends EntityLinkEvent
{
    public EntityLinkDeletedEvent(EntityLink entityLink, String localKey, Class<? extends EntityType> localType)
    {
        super(entityLink, localKey, localType);
    }

}
