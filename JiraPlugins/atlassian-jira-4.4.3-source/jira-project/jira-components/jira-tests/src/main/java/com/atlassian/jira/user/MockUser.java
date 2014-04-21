package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.embedded.api.UserWithAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.3
 */
public class MockUser implements UserWithAttributes
{
    private String name;
    private String fullName;
    private String email;
    private Map<String, Set<String>> values;

    public MockUser(final String username)
    {
        this(username, "", null);
    }

    public MockUser(final String username, final String fullName, final String email)
    {
        this(username, fullName, email, null);
    }

    public MockUser(final String username, final String fullName, final String email, Map<String, Set<String>> values)
    {
        this.name = username;
        this.fullName = fullName;
        this.email = email;
        if (values == null)
        {
            this.values = new HashMap<String, Set<String>>();
        }
        else
        {
            this.values = values;
        }
    }

    public boolean isActive()
    {
        return true;
    }

    public String getEmailAddress()
    {
        return email;
    }

    public String getDisplayName()
    {
        return fullName;
    }

    public long getDirectoryId()
    {
        return 1;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "<User " + name + ">";
    }

    public Set<String> getValues(final String key)
    {
        return values.get(key);
    }

    public String getValue(final String key)
    {
        Set<String> allValues = values.get(key);
        if (allValues != null && allValues.size() > 0)
        {
            return allValues.iterator().next();
        }
        return null;
    }

    public Set<String> getKeys()
    {
        return values.keySet();
    }

    public boolean isEmpty()
    {
        return values.size() == 0;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof User) && UserComparator.equal(this, (User) o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(final com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }
}
