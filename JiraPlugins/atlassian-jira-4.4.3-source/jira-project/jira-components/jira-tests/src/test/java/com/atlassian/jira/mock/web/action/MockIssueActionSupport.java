/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.web.action;

import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.web.action.IssueActionSupport;

import java.util.Collection;
import java.util.Map;

/**
 * A mock class that extends IssueActionSupport (and hence ProjectActionSupport and JiraWebActionSupport)
 *
 * @see IssueActionSupport
 * @see com.atlassian.jira.web.action.ProjectActionSupport
 * @see com.atlassian.jira.web.action.JiraWebActionSupport
 */
public class MockIssueActionSupport extends IssueActionSupport implements OperationContext
{
    private Map cfvh;

    /**
     * Implement this here just to use it in the custom field tests
     *
     * @see com.atlassian.jira.issue.customfields.TestCustomFieldUtils
     */
    public Map getFieldValuesHolder()
    {
        return cfvh;
    }

    public void setCustomFieldValuesHolder(Map customFieldValuesHolder)
    {
        cfvh = customFieldValuesHolder;
    }

    public Collection getCustomFieldValues(CustomField field)
    {
        return null;
    }

    public IssueOperation getIssueOperation()
    {
        return null;
    }
}
