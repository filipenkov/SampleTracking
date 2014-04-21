package com.atlassian.crowd.integration.rest.entity;

import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupComparator;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * Represents a Group entity.
 *
 * @since v2.1
 */
@XmlRootElement (name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupEntity implements GroupWithAttributes, Comparable<Group>
{
    @XmlAttribute (name = "name")
    private String name;

    @XmlElement (name = "description")
    private String description;

    @XmlElement
    private final GroupType type;

    @XmlElement (name = "active")
    private boolean active;

    @XmlElement(name = "attributes")
    private MultiValuedAttributeEntityList attributes;

    /**
     * JAXB requires a no-arg constructor.
     */
    private GroupEntity()
    {
        this.name = null;
        this.description = null;
        this.type = null;
        this.active = false;
    }

    public GroupEntity(final String name, final String description, final GroupType type, final boolean active)
    {
        this.name = name;
        this.description = description;
        this.type = type;
        this.active = active;
    }

    public String getDescription()
    {
        return description;
    }

    public GroupType getType()
    {
        return type;
    }

    public boolean isActive()
    {
        return active;
    }

    public long getDirectoryId()
    {
        return 0;
    }

    public String getName()
    {
        return name;
    }

    public void setAttributes(final MultiValuedAttributeEntityList attributes)
    {
        this.attributes = attributes;
    }

    public MultiValuedAttributeEntityList getAttributes()
    {
        return attributes;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", getName()).
                append("active", isActive()).
                append("description", getDescription()).
                append("type", getType()).
                toString();
    }

    public Set<String> getValues(String key)
    {
        return attributes.getValues(key);
    }

    public String getValue(String key)
    {
        return attributes.getValue(key);
    }

    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public int compareTo(Group o)
    {
        return GroupComparator.compareTo(this, o);
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

    /**
     * Creates a new minimal group instance.
     *
     * @param groupName group name
     * @return minimal group instance
     */
    public static GroupEntity newMinimalInstance(String groupName)
    {
        return new GroupEntity(groupName, null, null, false);
    }
}
