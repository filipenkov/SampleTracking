package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.model.EntityWithAttributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;

import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the concept of user which has attributes.
 */
public class InternalUserWithAttributes extends EntityWithAttributes implements UserWithAttributes
{
    private final InternalUser user;

    public InternalUserWithAttributes(final InternalUser user, final Map<String, Set<String>> attributes)
    {
        super(attributes);
        this.user = user;
    }

    public long getDirectoryId()
    {
        return user.getDirectoryId();
    }

    public String getName()
    {
        return user.getName();
    }

    public boolean isActive()
    {
        return user.isActive();
    }

    public String getEmailAddress()
    {
        return user.getEmailAddress();
    }

    public String getFirstName()
    {
        return user.getFirstName();
    }

    public String getLastName()
    {
        return user.getLastName();
    }

    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    public InternalUser getInternalUser()
    {
        return user;
    }

    public PasswordCredential getCredential()
    {
        return user.getCredential();
    }

    @Override
    public boolean equals(final Object o)
    {
        return UserComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(com.atlassian.crowd.embedded.api.User o)
    {
        return UserComparator.compareTo(this, o);
    }
}
