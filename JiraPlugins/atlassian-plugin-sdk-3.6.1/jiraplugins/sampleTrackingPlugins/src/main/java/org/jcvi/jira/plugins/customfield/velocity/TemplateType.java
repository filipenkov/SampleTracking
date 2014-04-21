package org.jcvi.jira.plugins.customfield.velocity;

import org.jcvi.jira.plugins.customfield.shared.config.ConfigurationParameter;

/**
 * User: pedworth
 * Date: 11/14/11
 * <p>TemplateTypes represent a particular use of a template.
 * Each templateType has a velocity template associated with
 * it, which is set in the configuration screen.</p>
 * <p>The class implements ConfigurationParameter so that
 * the TemplateTypes can be listed on the config screen</p>
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum TemplateType implements ConfigurationParameter {
    //UnusedDeclaration suppressed as they are used, just
    //not directly. They are accessed via enum.values()
    EDIT("Edit"),
    VIEW("View"),
    XML("XML"),
    SEARCH("Search"),
    COLUMN_VIEW("Search Column");

    public static final String SET     = "set";
    public static final String NOT_SET = "not set";

    private final String displayName;

    private TemplateType(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String getStorageKey() {
        return this.name(); //EDIT,VIEW etc
    }

    @Override
    public String getDisplayName() {
        return displayName; //Edit, View etc
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

    @Override
    public String toString() {
        return getDisplayName();
    }
}
