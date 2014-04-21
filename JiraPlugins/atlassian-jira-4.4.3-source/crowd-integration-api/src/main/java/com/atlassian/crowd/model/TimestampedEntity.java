package com.atlassian.crowd.model;

import java.util.Date;

/**
 * Some extra methods to add "created date" and "updated date" to Users and Groups.
 * Currently this is only applicable to Internal Entities.
 */
public interface TimestampedEntity
{
    /**
     * Returns the date the entity was created.
     *
     * @return date the entity was created
     */
    public Date getCreatedDate();

    /**
     * Returns the date the entity was last updated.
     *
     * @return date the entity was last updated.
     */
    public Date getUpdatedDate();    
}
