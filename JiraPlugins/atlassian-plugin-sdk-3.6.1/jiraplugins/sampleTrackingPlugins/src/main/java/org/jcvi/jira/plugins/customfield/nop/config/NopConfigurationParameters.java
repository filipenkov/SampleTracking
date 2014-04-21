package org.jcvi.jira.plugins.customfield.nop.config;

import org.jcvi.jira.plugins.customfield.shared.config.ConfigurationParameter;

/**
 * User: pedworth
 * Date: 11/3/11
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum NopConfigurationParameters implements ConfigurationParameter{
    //UnusedDeclaration suppressed as they are used, but via enum.values()
    OPTION1("OPTION1","Option 1"),
    OPTION2("OPTION2","Option 2");

    public static final String SET     = "set";
    public static final String NOT_SET = "not set";

    private final String key, name;

    private NopConfigurationParameters(String storageKey, String displayName) {
        this.key = storageKey;
        this.name = displayName;
    }
    @Override
    public String getStorageKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getSummary(String configValue) {
        if (configValue != null && configValue.trim().length() > 0) {
            return SET;
        } else {
            return NOT_SET;
        }
    }

    @Override
    public boolean isDisplayedInView() {
        return true;
    }
}
