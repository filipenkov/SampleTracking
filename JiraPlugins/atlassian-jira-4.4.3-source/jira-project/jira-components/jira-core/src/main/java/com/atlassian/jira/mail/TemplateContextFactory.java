/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;

public interface TemplateContextFactory
{
    public TemplateContext getTemplateContext(IssueEvent issueEvent);
}
