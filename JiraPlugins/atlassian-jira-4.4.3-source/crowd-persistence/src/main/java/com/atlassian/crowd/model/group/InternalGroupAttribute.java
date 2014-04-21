package com.atlassian.crowd.model.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.InternalEntityAttribute;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Encapsulates the concept of group attribute.
 */
public class InternalGroupAttribute extends InternalEntityAttribute
{
    private Directory directory;
    private InternalGroup group;

    protected InternalGroupAttribute()
    {
    }

    public InternalGroupAttribute(Long id, InternalGroup group, String name, String value)
    {
        this(group, name, value);
        setId(id);
    }


    public InternalGroupAttribute(final InternalGroup group, final String name, final String value)
    {
        super(name, value);

        this.group = group;
        this.directory = group.getDirectory();
    }

    public InternalGroup getGroup()
    {
        return group;
    }

    public Directory getDirectory()
    {
        return directory;
    }

    private void setGroup(final InternalGroup group)
    {
        this.group = group;
    }

    private void setDirectory(final Directory directory)
    {
        this.directory = directory;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof InternalGroupAttribute))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        InternalGroupAttribute that = (InternalGroupAttribute) o;

        if (getDirectory().getId() != null ? !getDirectory().getId().equals(that.getDirectory().getId()) : that.getDirectory().getId() != null)
        {
            return false;
        }
        if (getGroup().getId() != null ? !getGroup().getId().equals(that.getGroup().getId()) : that.getGroup().getId() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (getDirectory().getId() != null ? getDirectory().getId().hashCode() : 0);
        result = 31 * result + (getGroup().getId() != null ? getGroup().getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("directory", directory).
                append("group", group).
                toString();
    }
}
