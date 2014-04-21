package com.atlassian.jira.pageobjects.project.fields;

/**
 * Represents a field within a specific field configuration on the fields tab
 *
 * @since v4.4
 */
public interface Field
{
    String getName();

    String getDescription();

    boolean isRequired();

    String getRenderer();

    String getScreens();

    ScreensDialog openScreensDialog();
}
