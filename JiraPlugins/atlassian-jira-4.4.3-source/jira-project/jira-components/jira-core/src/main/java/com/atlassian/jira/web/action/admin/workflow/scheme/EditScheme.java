/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractEditScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class EditScheme extends AbstractEditScheme
{
    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getWorkflowSchemeManager();
    }

    public String getRedirectURL()
    {
        return "ViewWorkflowSchemes.jspa";
    }
}
