package com.atlassian.crowd.embedded.admin.crowd;

import com.atlassian.crowd.embedded.api.OperationType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.crowd.embedded.api.OperationType.*;

/**
 * Options in the UI for different types of permission configurations for an Crowd directory.
 */
public enum CrowdPermissionOption
{
    READ_ONLY(UPDATE_USER_ATTRIBUTE, UPDATE_GROUP_ATTRIBUTE),
    READ_WRITE(OperationType.values());

    private final List<OperationType> operationTypes;

    CrowdPermissionOption(OperationType... operationTypes)
    {
        this.operationTypes = Arrays.asList(operationTypes);
    }

    public Set<OperationType> getAllowedOperations()
    {
        return new HashSet<OperationType>(operationTypes);
    }

    public static CrowdPermissionOption fromAllowedOperations(Set<OperationType> allowedOperations)
    {
        if (allowedOperations.containsAll(READ_WRITE.operationTypes))
            return READ_WRITE;
        return READ_ONLY;
    }
}
