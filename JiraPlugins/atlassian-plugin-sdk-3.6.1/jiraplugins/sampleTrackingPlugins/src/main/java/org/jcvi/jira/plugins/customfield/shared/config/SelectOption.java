package org.jcvi.jira.plugins.customfield.shared.config;
/**
 * A trivial wrapper for the data used to construct an 'option' tag in
 * a 'select' tag area
 */
public class SelectOption {
    private final String formKey,displayName;
    public SelectOption(String key, String name) {
        this.formKey = key;
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormKey() {
        return formKey;
    }
}
