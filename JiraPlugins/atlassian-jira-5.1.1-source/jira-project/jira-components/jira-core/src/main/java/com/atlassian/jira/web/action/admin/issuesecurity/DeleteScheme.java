/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    protected void doValidation()
    {
        super.doValidation();

        // See if current errors already exist
        if (!invalidInput())
        {
            try
            {
                List projects = getProjects(getScheme());
                if (projects != null && !projects.isEmpty())
                {
                    addErrorMessage(getText("admin.errors.issuesecurity.cannot.delete.scheme"));
                }
            }
            catch (GenericEntityException e)
            {
                log.error("Error occured while retrieving projects for scheme.", e);
            }
        }
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getIssueSecuritySchemeManager();
    }

    public String getRedirectURL()
    {
        return "ViewIssueSecuritySchemes.jspa";
    }
}
