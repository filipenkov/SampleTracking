package com.atlassian.crowd.model.group;

import com.atlassian.crowd.model.LDAPDirectoryEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;

public class LDAPGroupWithAttributes implements GroupWithAttributes, LDAPDirectoryEntity
{
    private final String dn;
    private final String name;
    private final Long directoryId;
    private final GroupType type;
    private final boolean active;
    private final String description;

    private final Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();

    public LDAPGroupWithAttributes(String dn, GroupTemplateWithAttributes group)
    {
        Validate.isTrue(StringUtils.isNotBlank(dn));
        Validate.notNull(group, "group template cannot be null");
        Validate.notNull(group.getDirectoryId(), "directoryId cannot be null");
        Validate.notNull(group.getName(), "group name cannot be null");
        Validate.notNull(group.getType(), "group type cannot be null");

        this.dn = dn;
        this.directoryId = group.getDirectoryId();
        this.name = group.getName();
        this.active = group.isActive();
        this.type = group.getType();
        this.description = group.getDescription();

        // clone the attributes map
        for (Map.Entry<String, Set<String>> entry : group.getAttributes().entrySet())
        {
            attributes.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }
    }

    public String getDn()
    {
        return dn;
    }

    public String getName()
    {
        return name;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public GroupType getType()
    {
        return type;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getDescription()
    {
        return description;
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
        return GroupComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return GroupComparator.hashCode(this);
    }

    public int compareTo(Group group)
    {
        return GroupComparator.compareTo(this, group);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("dn", dn).
                append("name", name).
                append("directoryId", directoryId).
                append("type", type).
                append("active", active).
                append("description", description).
                append("attributes", attributes).
                toString();
    }
}
