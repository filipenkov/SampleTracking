/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractViewSchemes;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

@WebSudoRequired
public class ViewSchemes extends AbstractViewSchemes
{
    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getIssueSecuritySchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }

    public boolean isCanDelete(GenericValue scheme)
    {
        try
        {
            if (scheme != null)
            {
                List projects = getProjects(scheme);
                if (projects == null || projects.isEmpty())
                {
                    return true;
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error while retrieving projects for scheme.", e);
        }

        return false;
    }
}
