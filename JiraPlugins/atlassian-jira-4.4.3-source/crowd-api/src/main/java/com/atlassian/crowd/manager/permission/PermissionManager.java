package com.atlassian.crowd.manager.permission;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.embedded.api.OperationType;

/**
 * Permission Manager for Crowd to validate Directory Permissions and
 * Application-Directory permissions.
 */
public interface PermissionManager
{
    /**
     * Determine whether a directory has the permission to
     * perform a certain operation.
     *
     * @param directory     the directory to validate the permission against.
     * @param operationType the OperationType to check against.
     * @return true if and only if the directory is allowed to perform this operation.
     */
    boolean hasPermission(Directory directory, OperationType operationType);

    /**
     * Determine whether an application has permission to execute a particular
     * operation on a given directory.
     *
     * @param application   application that wants to perform the operation.
     * @param directory     directory to perform the operation on.
     * @param operationType type of operation to perform.
     * @return true if the application & directory has this permission, false otherwise.
     */
    boolean hasPermission(Application application, Directory directory, OperationType operationType);

    /**
     * Removes a permission with the given <code>OperationType</code> from the Application+Directory mapping
     *
     * @param application   application that wants to perform the operation.
     * @param directory     directory to forbid the operation on.
     * @param operationType type of operation to forbid.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void removePermission(Application application, Directory directory, OperationType operationType)
            throws ApplicationNotFoundException;

    /**
     * Adds a permission for the given <code>OperationType</code> to an Application+Directory mapping.
     *
     * @param application   application that wants to perform the operation.
     * @param directory     directory to allow the operation on.
     * @param operationType type of operation to forbid.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    void addPermission(Application application, Directory directory, OperationType operationType)
            throws ApplicationNotFoundException;

    void removePermission(Directory directory, OperationType operationType) throws DirectoryNotFoundException;

    void addPermission(Directory directory, OperationType operationType) throws DirectoryNotFoundException;
}