package com.atlassian.core.bean;

import com.atlassian.core.util.Clock;

import java.util.Date;

/**
 * Common superclass for persistent entities: provides a long key, and creation/modification
 * dates. Also provides a clock for testing.
 */
public class EntityObject implements Cloneable
{
    private long id = 0;
    private Date creationDate;
    private Date lastModificationDate;

    private Clock clock;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public Date getLastModificationDate()
    {
        return lastModificationDate != null ? lastModificationDate : creationDate;
    }

    public void setLastModificationDate(Date lastModificationDate)
    {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * The clock is used to fool the entity into thinking that the current time is different
     * to what it actually is. Used in tests so we get consistent results with time-based
     * activities.
     */
    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    /**
     * Consult the clock to get the current date.
     *
     * @return the current date as per the clock set in #setClock, or new Date() if
     *         no clock is set
     */
    public Date getCurrentDate()
    {
        if (clock != null)
            return clock.getCurrentDate();

        return new Date();
    }

    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof EntityObject)) return false;

        final EntityObject entityObject = (EntityObject) o;

        if (id != entityObject.getId()) return false;

        return true;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
