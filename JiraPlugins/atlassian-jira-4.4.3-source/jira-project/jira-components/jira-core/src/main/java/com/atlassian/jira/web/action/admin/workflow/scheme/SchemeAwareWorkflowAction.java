/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractSchemeAwareAction;
import com.atlassian.jira.scheme.SchemeManager;

public class SchemeAwareWorkflowAction extends AbstractSchemeAwareAction
{
    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getWorkflowSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }
}
