package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.*;
import com.atlassian.crowd.model.LDAPDirectoryEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;

public class LDAPUserWithAttributes implements UserWithAttributes, LDAPDirectoryEntity
{
    private final String dn;
    private final Long directoryId;
    private final String name;
    private final boolean active;
    private final String emailAddress;
    private final String firstName;
    private final String lastName;
    private final String displayName;

    private final Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();

    public LDAPUserWithAttributes(String dn, UserTemplateWithAttributes user)
    {
        Validate.isTrue(StringUtils.isNotBlank(dn));
        Validate.notNull(user, "user template cannot be null");
        Validate.notNull(user.getDirectoryId(), "directoryId cannot be null");
        Validate.notNull(user.getName(), "user name cannot be null");

        this.dn = dn;
        this.directoryId = user.getDirectoryId();
        this.name = user.getName();
        this.active = user.isActive();
        this.emailAddress = user.getEmailAddress();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.displayName = user.getDisplayName() == null ? "" : user.getDisplayName();

        // clone the attributes map
        for (Map.Entry<String, Set<String>> entry : user.getAttributes().entrySet())
        {
            attributes.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }
    }

    public String getDn()
    {
        return dn;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public Set<String> getValues(String name)
    {
        if (attributes.containsKey(name))
        {
            return Collections.unmodifiableSet(attributes.get(name));
        }
        else
        {
            return Collections.emptySet();
        }
    }

    public String getValue(String name)
    {
        Set<String> values = getValues(name);
        if (!values.isEmpty())
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    public Set<String> getKeys()
    {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
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

    public int compareTo(com.atlassian.crowd.embedded.api.User other)
    {
        return UserComparator.compareTo(this, other);
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("dn", dn).
                append("directoryId", directoryId).
                append("name", name).
                append("active", active).
                append("emailAddress", emailAddress).
                append("firstName", firstName).
                append("lastName", lastName).
                append("displayName", displayName).
                append("attributes", attributes).
                toString();
    }
}
