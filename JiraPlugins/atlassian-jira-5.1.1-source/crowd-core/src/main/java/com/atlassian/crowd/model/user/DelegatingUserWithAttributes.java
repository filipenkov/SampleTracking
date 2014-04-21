package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.UserComparator;

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
public class DelegatingUserWithAttributes extends com.atlassian.crowd.embedded.impl.DelegatingUserWithAttributes implements UserWithAttributes
{
    private final User user;

    public DelegatingUserWithAttributes(User user, Attributes attributes)
    {
        super(user, attributes);
        this.user = user;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Implementation of User
    // -----------------------------------------------------------------------------------------------------------------

    public String getFirstName()
    {
        return user.getFirstName();
    }

    public String getLastName()
    {
        return user.getLastName();
    }

    @Override
    public boolean equals(final Object o)
    {
        return user.equals(o);
    }

    @Override
    public int hashCode()
    {
        return user.hashCode();
    }

    @Override
    public int compareTo(final com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }
}
