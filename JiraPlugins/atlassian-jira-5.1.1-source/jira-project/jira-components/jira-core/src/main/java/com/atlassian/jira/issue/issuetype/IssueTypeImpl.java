/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.issuetype;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericValue;

public class IssueTypeImpl extends IssueConstantImpl implements IssueType
{
    public IssueTypeImpl(GenericValue genericValue, TranslationManager translationManager, JiraAuthenticationContext authenticationContext)
    {
        super(genericValue, translationManager, authenticationContext);
    }

    public boolean isSubTask()
    {
        return ComponentManager.getInstance().getSubTaskManager().isSubTaskIssueType(genericValue);
    }

    public String getType()
    {
        if (isSubTask())
        {
            return "Sub-Task";
        }
        else
        {
            return "Standard";
        }
    }
}
