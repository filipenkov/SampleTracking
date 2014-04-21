package com.atlassian.crowd.manager.permission;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.embedded.api.OperationType;
import static com.google.common.base.Preconditions.checkNotNull;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Implementation of the {@see PermissionManager}
 */
public class PermissionManagerImpl implements PermissionManager
{
    private static final Logger logger = Logger.getLogger(PermissionManagerImpl.class);

    private final ApplicationDAO applicationDao;
    private final DirectoryDao directoryDao;

    public PermissionManagerImpl(ApplicationDAO applicationDao, DirectoryDao directoryDao)
    {
        this.applicationDao = checkNotNull(applicationDao);
        this.directoryDao = checkNotNull(directoryDao);
    }

    public boolean hasPermission(final Directory directory, final OperationType operationType)
    {
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        boolean permission = directory.getAllowedOperations().contains(operationType);

        if (!permission && logger.isDebugEnabled())
        {
            logger.debug("Directory " + directory.getName() + " : Permission " + operationType.name() + " has been denied");
        }

        return permission;
    }

    public boolean hasPermission(final Application application, final Directory directory, final OperationType operationType)
    {
        Validate.notNull(application, "application cannot be null");
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        boolean hasPermission = false;

        if (hasPermission(directory, operationType))
        {
            DirectoryMapping mapping = application.getDirectoryMapping(directory.getId());
            if (mapping != null)
            {
                hasPermission = mapping.getAllowedOperations().contains(operationType);
            }
        }

        return hasPermission;
    }

    public void removePermission(final Directory directory, final OperationType operationType)
            throws DirectoryNotFoundException
    {
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        directory.getAllowedOperations().remove(operationType);
        directoryDao.update(directory);
    }

    public void removePermission(final Application application, final Directory directory, final OperationType operationType)
            throws ApplicationNotFoundException
    {
        Validate.notNull(application, "application cannot be null");
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        DirectoryMapping mapping = application.getDirectoryMapping(directory.getId());
        if (mapping != null)
        {
            mapping.getAllowedOperations().remove(operationType);
            applicationDao.update(application);
        }
    }

    public void addPermission(final Directory directory, final OperationType operationType)
            throws DirectoryNotFoundException
    {
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        directory.getAllowedOperations().add(operationType);
        directoryDao.update(directory);
    }

    public void addPermission(final Application application, final Directory directory, final OperationType operationType)
            throws ApplicationNotFoundException
    {
        Validate.notNull(application, "application cannot be null");
        Validate.notNull(directory, "directory cannot be null");
        Validate.notNull(operationType, "operationType cannot be null");

        DirectoryMapping mapping = application.getDirectoryMapping(directory.getId());
        if (mapping != null)
        {
            mapping.getAllowedOperations().add(operationType);
            applicationDao.update(application);
        }
    }
}
