package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;

/**
 * Implementation of UserWithAttributes that simply delegates to an underlying User and Attributes object.
 * <p>
 * Instances of this class will be effectively immutable so long as either:
 * <ul>
 * <li>It is constructed with immutable objects.</li>
 * or,
 * <li>The mutable objects it is constructed with are not "leaked".</li>
 * </ul>
 */
public class DelegatingUserWithAttributes extends AbstractDelegatingEntityWithAttributes implements UserWithAttributes
{
    private final User user;

    public DelegatingUserWithAttributes(User user, Attributes attributes)
    {
        super(attributes);
        this.user = user;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Implementation of User
    // -----------------------------------------------------------------------------------------------------------------

    public long getDirectoryId()
    {
        return user.getDirectoryId();
    }

    public boolean isActive()
    {
        return user.isActive();
    }

    public String getEmailAddress()
    {
        return user.getEmailAddress();
    }

    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    public int compareTo(final User user)
    {
        return this.user.compareTo(user);
    }

    public String getName()
    {
        return user.getName();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // equals() and hashCode()
    // -----------------------------------------------------------------------------------------------------------------

    @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
    public boolean equals(Object o)
    {
        return user.equals(o);
    }

    public int hashCode()
    {
        return user.hashCode();
    }
}
