package com.atlassian.crowd.embedded.directory;

import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.model.directory.DirectoryImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for setting attributes for a "Remote Crowd Server" Directory.
 *
 * This class is not thread safe.
 */
public class CrowdDirectoryAttributes
{
    public static final String APPLICATION_NAME = "application.name";
    public static final String APPLICATION_PASSWORD = "application.password";
    public static final String CROWD_SERVER_URL = "crowd.server.url";

    private String applicationName;
    private String applicationPassword;
    private String crowdServerUrl;
    private boolean nestedGroupsEnabled;
    private boolean incrementalSyncEnabled;
    private String crowdServerSynchroniseIntervalInSeconds;

    //----------------------------------------------------------------------
    // Getters and Setters
    //----------------------------------------------------------------------

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(final String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getApplicationPassword()
    {
        return applicationPassword;
    }

    public void setApplicationPassword(final String applicationPassword)
    {
        this.applicationPassword = applicationPassword;
    }

    public String getCrowdServerUrl()
    {
        return crowdServerUrl;
    }

    public void setCrowdServerUrl(final String crowdServerUrl)
    {
        this.crowdServerUrl = crowdServerUrl;
    }

    public boolean isNestedGroupsEnabled()
    {
        return nestedGroupsEnabled;
    }

    public void setNestedGroupsEnabled(final boolean nestedGroupsEnabled)
    {
        this.nestedGroupsEnabled = nestedGroupsEnabled;
    }

    public boolean isIncrementalSyncEnabled()
    {
        return incrementalSyncEnabled;
    }

    public void setIncrementalSyncEnabled(boolean incrementalSyncEnabled)
    {
        this.incrementalSyncEnabled = incrementalSyncEnabled;
    }

    public String getCrowdServerSynchroniseIntervalInSeconds()
    {
        return crowdServerSynchroniseIntervalInSeconds;
    }

    public void setCrowdServerSynchroniseIntervalInSeconds(final String crowdServerSynchroniseIntervalInSeconds)
    {
        this.crowdServerSynchroniseIntervalInSeconds = crowdServerSynchroniseIntervalInSeconds;
    }

//----------------------------------------------------------------------
    // Converters
    //----------------------------------------------------------------------

    public Map<String, String> toAttributesMap()
    {
        HashMap<String, String> map = new HashMap<String, String>(3);
        map.put(APPLICATION_NAME, applicationName);
        map.put(APPLICATION_PASSWORD, applicationPassword);
        map.put(CROWD_SERVER_URL, crowdServerUrl);
        map.put(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, String.valueOf(nestedGroupsEnabled));
        map.put(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED, String.valueOf(incrementalSyncEnabled));
        map.put(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL, crowdServerSynchroniseIntervalInSeconds);
        return map;
    }

    public static CrowdDirectoryAttributes fromAttributesMap(Map<String, String> map)
    {
        final CrowdDirectoryAttributes attributes = new CrowdDirectoryAttributes();
        attributes.setApplicationName(map.get(APPLICATION_NAME));
        attributes.setApplicationPassword(map.get(APPLICATION_PASSWORD));
        attributes.setCrowdServerUrl(map.get(CROWD_SERVER_URL));
        attributes.setNestedGroupsEnabled(Boolean.valueOf(map.get(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS)));
        attributes.setIncrementalSyncEnabled(Boolean.valueOf(map.get(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)));
        attributes.setCrowdServerSynchroniseIntervalInSeconds(map.get(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL));

        return attributes;
    }
}
