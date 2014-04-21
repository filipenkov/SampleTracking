package com.atlassian.crowd.model;

import com.atlassian.crowd.util.InternalEntityUtils;
import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.Date;

/**
 * Superclass for internally stored data objects.
 */
public abstract class InternalEntity implements Serializable, TimestampedEntity
{
    protected Long id;
    protected String name;
    protected boolean active;
    protected Date createdDate;
    protected Date updatedDate;

    protected InternalEntity()
    {
        this.id = null;
        this.createdDate = null;
        this.updatedDate = null;
        this.active = true;
    }

    protected InternalEntity(InternalEntityTemplate template)
    {
        setId(template.getId());
        setName(template.getName());
        setActive(template.isActive());
        setCreatedDate(template.getCreatedDate());
        setUpdatedDate(template.getUpdatedDate());
    }

    // MUTATOR
    public void setUpdatedDateToNow()
    {
        this.updatedDate = new Date();
    }

    // MUTATOR
    public void setCreatedDateToNow()
    {
        this.createdDate = new Date();
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public boolean isActive()
    {
        return active;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    private void setId(final Long id)
    {
        this.id = id;
    }

    protected void setName(String name)
    {
        Validate.notNull(name);
        InternalEntityUtils.validateLength(name);
        this.name = name;
    }

    protected void setActive(boolean active)
    {
        this.active = active;
    }

    protected void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    protected void setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    abstract public int hashCode();

    abstract public boolean equals(final Object obj);
}
