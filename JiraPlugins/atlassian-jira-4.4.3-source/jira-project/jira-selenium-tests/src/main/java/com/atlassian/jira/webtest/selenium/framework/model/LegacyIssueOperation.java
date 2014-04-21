package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.jira.webtest.framework.model.DefaultIssueActions;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.webtest.ui.keys.KeySequence;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.ID;


/**
* Represents an operation that can be performed on an issue. This is not an ENUM because the list is not fixed.
*
* @since v4.2
*/
public final class LegacyIssueOperation implements IssueOperation
{
    public static final LegacyIssueOperation ATTACH_FILE = new LegacyIssueOperation(DefaultIssueActions.ATTACH_FILES);
    public static final LegacyIssueOperation LOG_WORK = new LegacyIssueOperation(DefaultIssueActions.LOG_WORK);
    public static final LegacyIssueOperation LINK_ISSUE = new LegacyIssueOperation(DefaultIssueActions.LINK_ISSUE);
    public static final LegacyIssueOperation EDIT_LABELS = new LegacyIssueOperation(DefaultIssueActions.EDIT_LABELS);
    public static final LegacyIssueOperation COMMENT = new LegacyIssueOperation(null, "Comment");

    private final IssueOperation action;

    private final String name;
    private final String locator;

    // TODO name is the main parameter, rest may be null
    // TODO add shortcut handling (hasShortcut/shortcut etc.)

    public LegacyIssueOperation(IssueOperation action)
    {
        this.action = action;
        this.name = action.uiName();
        this.locator = ID.create(action.id());
    }

    public LegacyIssueOperation(String locator, String name)
    {
        this.action = null;
        this.name = notNull("name", name);
        this.locator = locator;
    }

    public boolean hasIssueMenuLocator()
    {
        return locator != null;
    }

    public String getViewIssueMenuLocator()
    {
        if (!hasIssueMenuLocator())
        {
            throw new IllegalStateException(this + " does not support opening from the issue menu");
        }

        return locator;
    }

    public String name()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return asString("IssueOperation[name=", name() ,"]");
    }

    @Override
    public String id()
    {
        return action.id();
    }

    @Override
    public String uiName()
    {
        return action.uiName();
    }

    @Override
    public String cssClass()
    {
        return action.cssClass();
    }

    @Override
    public boolean hasShortcut()
    {
        return action.hasShortcut();
    }

    @Override
    public KeySequence shortcut()
    {
        return action.shortcut();
    }
}
