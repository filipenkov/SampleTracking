/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.util;

import java.util.Collection;

/**
 * Used to provide the {@link SubTaskQuickCreationWebComponent} with configuration parameters.
 * For example, what fields should be displayed
 */
public interface SubTaskQuickCreationConfig
{
    /**
     * Returns a collection of field ids taht shodu be shown on the sub task quick creation form.
     * The collection uses empty strings to denote that an empty cell should be shown instead of a field.
     * See {@link com.atlassian.jira.issue.IssueFieldConstants} for possible field ids.
     *
     * @return collection of display field ids as String objects
     */
    Collection /* <String> */ getDisplayFieldIds();

    /**
     * Same as {@link #getDisplayFieldIds()}, only all the emty strings are stripped out of the returned collection.
     * This method is used by validation code.
     *
     * @return collection of field ids as String objects
     */
    Collection /* <String> */ getFieldIds();

    /**
     * Returns a collection of field ids that have default values.
     *
     * @return a collection of field ids that have default values.
     */
    Collection /* <String> */ getPresetFieldIds();

    /**
     * Returns the default field value
     *
     * @param fieldId field id
     * @return the default field value
     */
    String getPreset(String fieldId);

    /**
     * Returns the path to the template. The value comes from application properties.
     *
     * @return the path to the template
     * @see com.atlassian.jira.config.properties.APKeys#JIRA_SUBTASK_QUICKCREATE_TEMPLATE
     */
    String getVelocityTemplate();

    /**
     * Returns the i18n key for the given field id if such label for such field was defined, otherwise null.
     *
     * @param fieldId field id
     * @return the i18n key or null
     * @since v3.11
     */
    String getFieldI18nLabelKey(String fieldId);
}
