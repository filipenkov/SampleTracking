package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class ReleaseErrorReportingServiceImpl implements ReleaseErrorReportingService
{
    private static final Logger log = Logger.getLogger(ReleaseErrorReportingServiceImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final Function<String, ManagedLock.ReadWrite> lockManager = ManagedLocks.weakReadWriteManagedLockFactory();

    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final PluginSettingsFactory pluginSettingsFactory;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public ReleaseErrorReportingServiceImpl(final PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods

    public void recordError(final @NotNull String projectKey, final long versionId, final @NotNull String error)
    {
        lockManager.get(projectKey + versionId).write().withLock(new Runnable()
        {
            public void run()
            {
                List<String> errors = new ArrayList<String>();
                final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
                Object errorObject = settingsForKey.get(PluginConstants.PS_RELEASE_ERRORS + versionId);
                if (errorObject != null && List.class.isAssignableFrom(errorObject.getClass()))
                {
                    //noinspection unchecked
                    errors = (List<String>) errorObject;
                }

                errors.add(error);
                settingsForKey.put(PluginConstants.PS_RELEASE_ERRORS + versionId, errors);
            }
        });
    }

    public void recordErrors(@NotNull final String projectKey, final long versionId, @NotNull final List<String> errors)
    {
        if (!errors.isEmpty())
        {
            lockManager.get(projectKey + versionId).write().withLock(new Runnable()
            {
                public void run()
                {
                    for (String error : errors)
                    {
                        recordError(projectKey, versionId, error);
                    }
                }
            });
        }
    }

    @NotNull
    public List<String> getErrors(@NotNull final String projectKey, final long versionId)
    {
        try
        {
            return lockManager.get(projectKey + versionId).read().withLock(new Callable<List<String>>()
            {
                public List<String> call() throws Exception
                {
                    final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
                    Object errorObject = settingsForKey.get(PluginConstants.PS_RELEASE_ERRORS + versionId);
                    if (errorObject != null && List.class.isAssignableFrom(errorObject.getClass()))
                    {
                        //noinspection unchecked
                        return (List<String>) errorObject;
                    }

                    return Collections.emptyList();
                }
            });
        }
        catch (Exception e)
        {
            // should never happen there is nothing in our code which is throwing a checked exception anyway.
            log.error("An unexpected error has occurred", e);
            return Collections.emptyList();
        }
    }

    public void clearErrors(@NotNull final String projectKey, final long versionId)
    {
        lockManager.get(projectKey + versionId).write().withLock(new Runnable()
        {
            public void run()
            {
                final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
                settingsForKey.remove(PluginConstants.PS_RELEASE_ERRORS + versionId);
            }
        });
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
