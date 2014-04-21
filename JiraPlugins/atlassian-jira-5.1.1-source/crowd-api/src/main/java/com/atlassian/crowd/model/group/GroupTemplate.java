package com.atlassian.crowd.model.group;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * A publicly mutable Group implementation.
 * <p/>
 * Used to create or update a group.
 */
public class GroupTemplate implements Group, Serializable
{
    private String name;
    private long directoryId;
    private GroupType type;
    private boolean local;
    private boolean active;
    private String description;

    /**
     * Build a template for a new group.
     * <p/>
     * Used to create a group.
     *
     * @param name        group name of new group.
     * @param directoryId ID of the directory in which to store the new group.
     * @param type        the group type see {@link GroupType}
     */
    public GroupTemplate(String name, long directoryId, GroupType type)
    {
        Validate.isTrue(StringUtils.isNotBlank(name), "name argument cannot be null or blank");
        Validate.notNull(type, "type argument cannot be null");

        // lowercasing not enforced, only on the Internal User, since an LDAP user can handle both
        this.name = name;
        this.directoryId = directoryId;
        this.type = type;
        this.active = true;
    }

    public GroupTemplate(final String name)
    {
        this(name, -1L);
    }

    /**
     * Contructor that defaults the {@link com.atlassian.crowd.model.group.GroupType} to {@link com.atlassian.crowd.model.group.GroupType#GROUP}
     *
     * @param name        the name of the group
     * @param directoryId ID of the directory in which to store the new group.
     */
    public GroupTemplate(String name, long directoryId)
    {
        this(name, directoryId, GroupType.GROUP);
    }

    /**
     * Build a template from an existing group.
     * <p/>
     * Used to update a group.
     *
     * @param group group to build template from.
     */
    public GroupTemplate(Group group)
    {
        Validate.notNull(group, "group argument cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(group.getName()), "group.name argument cannot be null or blank");
        Validate.notNull(group.getType(), "group.type argument cannot be null");

        this.name = group.getName();
        this.directoryId = group.getDirectoryId();
        this.active = group.isActive();
        this.type = group.getType();
        this.description = group.getDescription();
    }

    public GroupTemplate(com.atlassian.crowd.embedded.api.Group group)
    {
        Validate.notNull(group, "group argument cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(group.getName()), "group.name argument cannot be null or blank");
        this.name = group.getName();
        this.type = GroupType.GROUP;
        this.active = true;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDirectoryId(long directoryId)
    {
        this.directoryId = directoryId;
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

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public GroupType getType()
    {
        return type;
    }

    public void setType(GroupType type)
    {
        this.type = type;
    }

    public boolean isLocal()
    {
        return local;
    }

    public void setLocal(final boolean local)
    {
        this.local = local;
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
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

    public int compareTo(Group other)
    {
        return GroupComparator.compareTo(this, other);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", name).
                append("directoryId", directoryId).
                append("active", active).
                append("type", type).
                append("description", description).
                toString();
    }
}
