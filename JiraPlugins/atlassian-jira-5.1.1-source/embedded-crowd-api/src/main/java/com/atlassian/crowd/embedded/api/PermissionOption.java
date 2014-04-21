package com.atlassian.crowd.embedded.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.crowd.embedded.api.OperationType.*;

/**
 * Options in the UI for different types of permission configurations for an LDAP directory.
 */
public enum PermissionOption
{
    READ_ONLY(UPDATE_USER_ATTRIBUTE, UPDATE_GROUP_ATTRIBUTE),
    READ_ONLY_LOCAL_GROUPS(UPDATE_USER_ATTRIBUTE, UPDATE_GROUP_ATTRIBUTE, CREATE_GROUP, UPDATE_GROUP, DELETE_GROUP),
    READ_WRITE(OperationType.values());

    private final List<OperationType> operationTypes;

    PermissionOption(OperationType... operationTypes)
    {
        this.operationTypes = Arrays.asList(operationTypes);
    }

    public Set<OperationType> getAllowedOperations()
    {
        return new HashSet<OperationType>(operationTypes);
    }

    public static PermissionOption fromAllowedOperations(Set<OperationType> allowedOperations)
    {
        if (allowedOperations.containsAll(READ_WRITE.operationTypes))
        {
            return READ_WRITE;
        }
        if (allowedOperations.containsAll(READ_ONLY_LOCAL_GROUPS.operationTypes))
        {
            return READ_ONLY_LOCAL_GROUPS;
        }
        return READ_ONLY;
    }
}