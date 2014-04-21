/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.jira.JiraException;

public interface IssueUpdater
{
    void doUpdate(IssueUpdateBean issueUpdateBean, boolean generateChangeItems) throws JiraException;
}
