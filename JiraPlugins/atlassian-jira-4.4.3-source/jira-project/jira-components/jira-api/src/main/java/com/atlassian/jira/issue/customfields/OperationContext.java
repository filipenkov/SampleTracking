/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.operation.IssueOperation;
import java.util.Map;

public interface OperationContext
{
    public Map getFieldValuesHolder();

    public IssueOperation getIssueOperation();
}