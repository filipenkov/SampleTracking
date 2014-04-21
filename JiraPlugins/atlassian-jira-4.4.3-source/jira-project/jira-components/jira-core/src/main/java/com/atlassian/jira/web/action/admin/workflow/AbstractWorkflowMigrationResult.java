/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWorkflowMigrationResult implements WorkflowMigrationResult
{
    protected final Map failedIssues;

    protected AbstractWorkflowMigrationResult(Map failedIssues)
    {
        if (failedIssues != null)
        {
            this.failedIssues = new HashMap(failedIssues);
        }
        else
        {
            this.failedIssues = Collections.EMPTY_MAP;
        }
    }

    public int getNumberOfFailedIssues()
    {
        return this.failedIssues.size();
    }

    public Map getFailedIssues()
    {
        return Collections.unmodifiableMap(this.failedIssues);
    }
}
