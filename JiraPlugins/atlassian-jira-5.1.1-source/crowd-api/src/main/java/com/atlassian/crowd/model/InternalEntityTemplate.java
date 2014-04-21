package com.atlassian.crowd.model;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * This class can be used to hold data for performing an XML import.
 */
public class InternalEntityTemplate
{
    private Long id;
    private String name;
    private boolean active;
    private Date createdDate;
    private Date updatedDate;

    public InternalEntityTemplate()
    {
    }

    public InternalEntityTemplate(Long id, String name, boolean active, Date createdDate, Date updatedDate)
    {
        setId(id);
        setName(name);
        setActive(active);
        setCreatedDate(createdDate);
        setUpdatedDate(updatedDate);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        Validate.notNull(name);
        this.name = name;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(final boolean active)
    {
        this.active = active;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public void setUpdatedDate(final Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        InternalEntityTemplate that = (InternalEntityTemplate) o;

        if (active != that.active)
        {
            return false;
        }
        if (createdDate != null ? !createdDate.equals(that.createdDate) : that.createdDate != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (updatedDate != null ? !updatedDate.equals(that.updatedDate) : that.updatedDate != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (updatedDate != null ? updatedDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("name", name).
                append("active", active).
                append("createdDate", createdDate).
                append("updatedDate", updatedDate).
                toString();
    }

    public String toFriendlyString()
    {
        return new StringBuilder().append("Username: ").append(name).append(", Created Date: ").append(createdDate).append(", Updated Date: ").append(updatedDate).toString();    
    }
}
