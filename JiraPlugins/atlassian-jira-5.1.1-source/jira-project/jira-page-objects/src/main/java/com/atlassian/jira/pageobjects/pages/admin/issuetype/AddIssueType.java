package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.components.IconPicker;

import java.util.Map;

/**
 * Interface for a page object to add an issue type.
 *
 * @since v5.0.1
 */
public interface AddIssueType
{
    AddIssueType setName(String name);
    AddIssueType setDescription(String description);
    AddIssueType setIconUrl(String iconUrl);
    AddIssueType setSubtask(boolean subtask);
    String getIconUrl();
    boolean isSubtasksEnabled();
    IconPicker.IconPickerPopup openIconPickerPopup();
    public Map<String, String> getFormErrors();
    <P> P submit(Class<P> klazz);
    <P> P cancel(Class<P> page);
    AddIssueType submitFail();
    <P> P submitFail(Class<P> page, Object... args);
}
