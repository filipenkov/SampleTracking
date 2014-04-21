package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.InternalEntityAttribute;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Encapsulates the concept of user attribute.
 */
public class InternalUserAttribute extends InternalEntityAttribute
{
    private Directory directory;
    private InternalUser user;

    protected InternalUserAttribute()
    {
    }

    public InternalUserAttribute(Long id, InternalUser user, String name, String value)
    {
        this(user, name, value);
        setId(id);
    }

    public InternalUserAttribute(InternalUser user, String name, String value)
    {
        super(name, value);

        this.user = user;
        this.directory = user.getDirectory();
    }

    public InternalUser getUser()
    {
        return user;
    }

    public Directory getDirectory()
    {
        return directory;
    }

    private void setUser(final InternalUser user)
    {
        this.user = user;
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
        if (!(o instanceof InternalUserAttribute))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        InternalUserAttribute that = (InternalUserAttribute) o;

        if (getDirectory().getId() != null ? !getDirectory().getId().equals(that.getDirectory().getId()) : that.getDirectory().getId() != null)
        {
            return false;
        }
        if (getUser().getId() != null ? !getUser().getId().equals(that.getUser().getId()) : that.getUser().getId() != null)
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
        result = 31 * result + (getUser().getId() != null ? getUser().getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("directory", directory).
                append("user", user).
                toString();
    }
}
