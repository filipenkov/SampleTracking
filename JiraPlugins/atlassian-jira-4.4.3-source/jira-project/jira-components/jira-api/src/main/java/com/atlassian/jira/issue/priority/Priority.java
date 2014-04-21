/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.priority;

import com.atlassian.jira.issue.IssueConstant;

public interface Priority extends IssueConstant
{
    String getStatusColor();

    void setStatusColor(String statusColor);
}
