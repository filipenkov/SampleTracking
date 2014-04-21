/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractAddScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class AddScheme extends AbstractAddScheme
{
    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getWorkflowSchemeManager();
    }

    public String getRedirectURL()
    {
        return "EditWorkflowSchemeEntities!default.jspa?schemeId=";
    }
}
