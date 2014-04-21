package com.atlassian.crowd.directory.cache.model;

import com.atlassian.crowd.model.LDAPDirectoryEntity;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;

import java.util.HashMap;
import java.util.Map;


/**
 * There are three types of entities: users and groups.
 */
public enum EntityType
{
    USER(LDAPUserWithAttributes.class),
    GROUP(LDAPGroupWithAttributes.class);

    private static final Map<Class<? extends LDAPDirectoryEntity>, EntityType> entityTypeMap = new HashMap<Class<? extends LDAPDirectoryEntity>, EntityType>();

    static
    {
        for (EntityType type : EntityType.values())
        {
            entityTypeMap.put(type.getLdapEntityClass(), type);
        }
    }

    private final Class<? extends LDAPDirectoryEntity> ldapEntityClass;

    EntityType(final Class<? extends LDAPDirectoryEntity> ldapEntityClass)
    {
        this.ldapEntityClass = ldapEntityClass;
    }

    public Class<? extends LDAPDirectoryEntity> getLdapEntityClass()
    {
        return ldapEntityClass;
    }

    public static EntityType valueOf(Class<? extends LDAPDirectoryEntity> entityClass)
    {
        final EntityType entityType = entityTypeMap.get(entityClass);
        if (entityType == null)
        {
            throw new IllegalArgumentException("Entity type class unsupported: " + entityClass.getCanonicalName());
        }
        return entityType;
    }
}
