package com.atlassian.labs.hipchat.components;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class ConfigurationManager {

    private static final String PLUGIN_STORAGE_KEY = "com.atlassian.labs.hipchat";
    private static final String HIPCHAT_AUTH_TOKEN_KEY = "hipchat-auth-token";

    private final PluginSettingsFactory pluginSettingsFactory;

    public ConfigurationManager(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public String getHipChatApiToken() {
        return (String) this.pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY).get(HIPCHAT_AUTH_TOKEN_KEY);
    }

    public void updateHipChatApiToken(String token) {
        this.pluginSettingsFactory.createSettingsForKey(PLUGIN_STORAGE_KEY).put(HIPCHAT_AUTH_TOKEN_KEY, token);
    }
}