/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.issuetype;

import com.atlassian.jira.issue.IssueConstant;

public interface IssueType extends IssueConstant
{
    boolean isSubTask();
}
