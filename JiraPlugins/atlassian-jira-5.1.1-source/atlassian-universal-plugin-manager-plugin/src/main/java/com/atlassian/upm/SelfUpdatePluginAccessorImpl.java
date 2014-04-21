package com.atlassian.upm;

import java.io.File;
import java.net.URI;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegates method calls to a dynamically loaded component from the self-update plugin.
 * See {@link SelfUpdateController}.
 */
public class SelfUpdatePluginAccessorImpl implements SelfUpdatePluginAccessor
{
    // The following constants must be kept in sync with the self-update plugin code -
    // see com.atlassian.upm.selfupdate.Constants.
    private static final String SELFUPDATE_SETTINGS_BASE = "com.atlassian.upm:selfupdate";
    private static final String SELFUPDATE_SETTINGS_JAR_PATH = SELFUPDATE_SETTINGS_BASE + ".jar";
    private static final String SELFUPDATE_SETTINGS_UPM_KEY = SELFUPDATE_SETTINGS_BASE + ".key";
    private static final String SELFUPDATE_SETTINGS_UPM_URI = SELFUPDATE_SETTINGS_BASE + ".upm.uri";
    private static final String SELFUPDATE_SETTINGS_SELFUPDATE_PLUGIN_URI = SELFUPDATE_SETTINGS_BASE + ".stub.uri";
    private static final String SELFUPDATE_EXECUTE_UPDATE_RESOURCE_PATH = "/rest/plugins/self-update/1.0/";
    
    private final ApplicationProperties applicationProperties;
    private final PluginSettingsFactory pluginSettingsFactory;

    public SelfUpdatePluginAccessorImpl(ApplicationProperties applicationProperties,
                                        PluginSettingsFactory pluginSettingsFactory)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
    }
    
    public URI prepareUpdate(File jarToInstall, String expectedPluginKey, URI pluginUri, URI selfUpdatePluginUri)
    {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        settings.put(SELFUPDATE_SETTINGS_JAR_PATH, jarToInstall.getAbsolutePath());
        settings.put(SELFUPDATE_SETTINGS_UPM_KEY, expectedPluginKey);
        settings.put(SELFUPDATE_SETTINGS_UPM_URI, pluginUri.toString());
        settings.put(SELFUPDATE_SETTINGS_SELFUPDATE_PLUGIN_URI, selfUpdatePluginUri.toString());
        
        return URI.create(applicationProperties.getBaseUrl() + SELFUPDATE_EXECUTE_UPDATE_RESOURCE_PATH);
    }
}
