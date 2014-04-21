package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.impl.DefaultConnectionPoolProperties;
import com.atlassian.crowd.embedded.impl.SystemConnectionPoolProperties;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class CrowdDirectoryServiceImpl implements CrowdDirectoryService
{
    private final Logger logger = Logger.getLogger(CrowdDirectoryServiceImpl.class);

    private final DirectoryManager directoryManager;
    private final ApplicationManager applicationManager;
    private final ApplicationFactory applicationFactory;
    private final DirectoryInstanceLoader directoryInstanceLoader;
    private volatile boolean ldapConnectionPoolSettingsApplied;

    public CrowdDirectoryServiceImpl(ApplicationFactory applicationFactory, DirectoryInstanceLoader directoryInstanceLoader, DirectoryManager directoryManager, ApplicationManager applicationManager)
    {
        this.directoryManager = checkNotNull(directoryManager);
        this.applicationManager = checkNotNull(applicationManager);
        this.applicationFactory = checkNotNull(applicationFactory);
        this.directoryInstanceLoader = checkNotNull(directoryInstanceLoader);
    }

    public Directory addDirectory(Directory directory) throws OperationFailedException
    {
        try
        {
            return directoryManager.addDirectory(directory);
        }
        catch (com.atlassian.crowd.exception.DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void testConnection(Directory directory) throws OperationFailedException
    {
        try
        {
            directoryInstanceLoader.getRawDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes()).testConnection();
        }
        catch (com.atlassian.crowd.exception.DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public List<Directory> findAllDirectories()
    {
        return Lists.transform(getApplication().getDirectoryMappings(), new Function<DirectoryMapping, Directory>()
        {
            public Directory apply(DirectoryMapping from)
            {
                return from.getDirectory();
            }
        });
    }

    public Directory findDirectoryById(long directoryId)
    {
        try
        {
            return directoryManager.findDirectoryById(directoryId);
        }
        catch (com.atlassian.crowd.exception.DirectoryNotFoundException e)
        {
            return null;
        }
    }

    public Directory updateDirectory(Directory directory) throws OperationFailedException
    {
        try
        {
            return directoryManager.updateDirectory(directory);
        }
        catch (com.atlassian.crowd.exception.DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void setDirectoryPosition(long directoryId, int position) throws OperationFailedException
    {
        try
        {
            applicationManager.updateDirectoryMapping(getApplication(), findDirectoryById(directoryId), position);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public boolean removeDirectory(long directoryId) throws DirectoryCurrentlySynchronisingException, OperationFailedException
    {
        final Directory directory = findDirectoryById(directoryId);

        if (directory != null)
        {
            try
            {
                directoryManager.removeDirectory(directory);
            }
            catch (com.atlassian.crowd.exception.DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
        }

        return findDirectoryById(directoryId) != null;
    }

    public boolean supportsNestedGroups(final long directoryId) throws OperationFailedException
    {
        try
        {
            return directoryManager.supportsNestedGroups(directoryId);
        }
        catch (DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public boolean isDirectorySynchronisable(final long directoryId) throws OperationFailedException
    {
        try
        {
            return directoryManager.isSynchronisable(directoryId);
        }
        catch (DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void synchroniseDirectory(final long directoryId) throws OperationFailedException
    {
        synchroniseDirectory(directoryId, true);
    }

    public void synchroniseDirectory(final long directoryId, boolean runInBackground) throws OperationFailedException
    {
        try
        {
            directoryManager.synchroniseCache(directoryId, SynchronisationMode.FULL, runInBackground);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean isDirectorySynchronising(final long directoryId) throws OperationFailedException
    {
        try
        {
            return directoryManager.isSynchronising(directoryId);
        }
        catch (DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public DirectorySynchronisationInformation getDirectorySynchronisationInformation(long directoryId)
            throws OperationFailedException
    {
        try
        {
            return directoryManager.getDirectorySynchronisationInformation(directoryId);
        }
        catch (DirectoryInstantiationException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void setConnectionPoolProperties(ConnectionPoolProperties poolProperties)
    {
        ApplicationImpl template = ApplicationImpl.newInstance(getApplication());
        template.getAttributes().putAll(poolProperties.toPropertiesMap());
        try
        {
            applicationManager.update(template);
        }
        catch (ApplicationManagerException e)
        {
            throw new RuntimeException(e);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ConnectionPoolProperties getStoredConnectionPoolProperties()
    {
        Map<String, String> attributes = getApplication().getAttributes();
        return DefaultConnectionPoolProperties.fromPropertiesMap(attributes);
    }

    private Application getApplication()
    {
        Application application = applicationFactory.getApplication();
        initialiseConnectionPoolSystemProperties(application); // TODO: remove when JIRA/Confluence publish ApplicationReadyEvent
        return application;
    }

    public ConnectionPoolProperties getSystemConnectionPoolProperties()
    {
        initialiseConnectionPoolSystemProperties(); // TODO: remove when JIRA/Confluence publish ApplicationReadyEvent
        return SystemConnectionPoolProperties.getInstance();
    }


    /*
    * We currently have initialiseConnectionPoolSystemProperties until the JIRA/Confluence themselves
    * publish ApplicationReadyEvent on startup so we handle and set the system properties.
    *
    * Once JIRA/Confluence publishes the event on startup we can remove the initialisation of connection pool system properties
    * from getApplication and getSystemConnectionPoolProperties as the LdapConnectionPoolInitialisationListener will handle
    * the setting of system properties.
    *
    * See:
    * http://jira.atlassian.com/browse/CWD-2121
    * https://studio.atlassian.com/browse/EMBCWD-649
    */
    private void initialiseConnectionPoolSystemProperties(Application application)
    {
        if (!ldapConnectionPoolSettingsApplied)
        {
            // Updates the LDAP connection pool system property with values from the database
            storeLdapConnectionPoolConfiguration(application);
            ldapConnectionPoolSettingsApplied = true;
        }
    }

    private void initialiseConnectionPoolSystemProperties()
    {
        initialiseConnectionPoolSystemProperties(getApplication());
    }

    /**
     * Sets the System properties with the connection pool configurations retrieved from the database,
     *
     * @param application the application with the connection pool properties
     */
    private void storeLdapConnectionPoolConfiguration(Application application)
    {
        Map<String, String> attributes = application.getAttributes();
        ConnectionPoolProperties connectionPoolConfiguration = DefaultConnectionPoolProperties.fromPropertiesMap(attributes);
        Map<String, String> propertiesMap = connectionPoolConfiguration.toPropertiesMap();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet())
        {
            if (entry.getValue() != null) // ignore property if value is null; values set by JVM or on command line (-D...) will take precedence
            {
                logger.debug("Setting system-wide LDAP connection pool property: <" + entry.getKey() + "> with value: <" + entry.getValue() + ">");
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
}
