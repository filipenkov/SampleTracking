package com.atlassian.crowd.embedded.api;

/**
 * Represents the type of operations allowed to be performed on a directory.
 */
public enum OperationType
{
    CREATE_USER,
    CREATE_GROUP,
    CREATE_ROLE,

    UPDATE_USER,
    UPDATE_GROUP,
    UPDATE_ROLE,

    UPDATE_USER_ATTRIBUTE,
    UPDATE_GROUP_ATTRIBUTE,
    UPDATE_ROLE_ATTRIBUTE,

    DELETE_USER,
    DELETE_GROUP,
    DELETE_ROLE;

    public String getName()
    {
        return name();
    }

    public OperationType fromName(String name)
    {
        return OperationType.valueOf(name);
    }
}
