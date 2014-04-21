package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.pageobjects.PageBinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * @since v5.1
 */
public class IssueActionsDialog extends FormDialog
{
    private static final By ISSUE_ACTIONS_QUERYABLE_CONTAINER = By.id("issueactions-queryable-container");
    private static final By ISSUE_ACTIONS = By.id("issueactions-suggestions");

    @Inject
    private PageBinder binder;

    public IssueActionsDialog()
    {
        super("issue-actions-dialog");
    }

    public AutoComplete getAutoComplete()
    {
        return binder.bind(QueryableDropdownSelect.class, ISSUE_ACTIONS_QUERYABLE_CONTAINER, ISSUE_ACTIONS);
    }

}
