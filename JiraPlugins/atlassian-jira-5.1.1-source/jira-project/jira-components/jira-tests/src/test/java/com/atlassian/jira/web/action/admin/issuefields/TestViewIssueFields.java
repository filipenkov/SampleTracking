/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

public class TestViewIssueFields extends AbstractTestViewIssueFields
{
    private ViewIssueFields viewIssueFields;

    public TestViewIssueFields(String s)
    {
        super(s);
    }

    public void setNewVif()
    {
        viewIssueFields = new ViewIssueFields(null, null, reindexMessageManager, fieldLayoutSchemeHelper, null, null);
    }

    public AbstractConfigureFieldLayout getVif()
    {
        if (viewIssueFields == null)
        {
            setNewVif();
        }
        return viewIssueFields;
    }

    public void refreshVif()
    {
        viewIssueFields = null;
    }
}
