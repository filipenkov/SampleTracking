package com.atlassian.crowd.search;

import com.atlassian.crowd.model.group.GroupType;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class EntityDescriptor
{
    private final static EntityDescriptor USER_DESCRIPTOR = new EntityDescriptor(Entity.USER);
    private final static EntityDescriptor DIRECTORY_DESCRIPTOR = new EntityDescriptor(Entity.DIRECTORY);
    private final static EntityDescriptor TOKEN_DESCRIPTOR = new EntityDescriptor(Entity.TOKEN);
    private final static EntityDescriptor APPLICATION_DESCRIPTOR = new EntityDescriptor(Entity.APPLICATION);
    private final static EntityDescriptor ALIAS_DESCRIPTOR = new EntityDescriptor(Entity.ALIAS);

    private final static Map<GroupType, EntityDescriptor> GROUP_DESCRIPTORS;

    static
    {
        GROUP_DESCRIPTORS = new HashMap<GroupType, EntityDescriptor>();
        for (GroupType groupType : GroupType.values())
        {
            GROUP_DESCRIPTORS.put(groupType, new EntityDescriptor(Entity.GROUP, groupType));
        }
        GROUP_DESCRIPTORS.put(null, new EntityDescriptor(Entity.GROUP, null));
    }

    private final Entity entityType;
    private final GroupType groupType; // can be null

    private EntityDescriptor(final Entity entityType, final GroupType groupType)
    {
        this.entityType = entityType;
        this.groupType = groupType;
    }

    private EntityDescriptor(final Entity entity)
    {
        this(entity, null);
    }

    public static EntityDescriptor group(GroupType groupType)
    {
        return GROUP_DESCRIPTORS.get(groupType);
    }

    public static EntityDescriptor group()
    {
        return group(GroupType.GROUP);
    }

    public static EntityDescriptor role()
    {
        return group(GroupType.LEGACY_ROLE);
    }

    public static EntityDescriptor user()
    {
        return USER_DESCRIPTOR;
    }

    public static EntityDescriptor directory()
    {
        return DIRECTORY_DESCRIPTOR;
    }

    public static EntityDescriptor token()
    {
        return TOKEN_DESCRIPTOR;
    }

    public static EntityDescriptor application()
    {
        return APPLICATION_DESCRIPTOR;
    }

    public static EntityDescriptor alias()
    {
        return ALIAS_DESCRIPTOR;
    }

    public Entity getEntityType()
    {
        return entityType;
    }

    public GroupType getGroupType()
    {
        return groupType;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityDescriptor that = (EntityDescriptor) o;

        if (entityType != that.entityType) return false;
        if (groupType != that.groupType) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = entityType != null ? entityType.hashCode() : 0;
        result = 31 * result + (groupType != null ? groupType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("entityType", entityType).
                append("groupType", groupType).
                toString();
    }
}
