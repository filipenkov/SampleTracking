/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.notification.ProjectAware;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public abstract class AbstractSelectProjectScheme extends AbstractSchemeAwareAction implements ProjectAware
{
    private String[] schemeIds = new String[] { "" };
    private Long projectId;
    private GenericValue project;

    public String doDefault() throws Exception
    {
        List schemes = getSchemeManager().getSchemes(getProject());
        if (schemes.size() != 0)
        {
            schemeIds = new String[schemes.size()];
            for (int i = 0; i < schemes.size(); i++)
            {
                GenericValue scheme = (GenericValue) schemes.get(i);
                schemeIds[i] = scheme.getLong("id").toString();
            }
        }
        if (hasPermission())
        {
            return super.doDefault();
        }
        else
        {
            return "securitybreach";
        }
    }

    protected void doValidation()
    {
        if (getProjectId() == null)
        {
            addErrorMessage("You must select a project for this scheme");
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getSchemeManager().removeSchemesFromProject(getProject());
        for (int i = 0; i < getSchemeIds().length; i++)
        {
            String s = getSchemeIds()[i];
            if (TextUtils.stringSet(s))
            {
                GenericValue scheme = getSchemeManager().getScheme(new Long(s));
                getSchemeManager().addSchemeToProject(getProject(), scheme);
            }
        }

        if (hasPermission())
        {
            return getRedirect(getProjectReturnUrl());
        }
        else
        {
            return "securitybreach";
        }
    }

    protected String getProjectReturnUrl() throws GenericEntityException
    {
        return "/plugins/servlet/project-config/" + getProject().getString("key") + "/summary";
    }

    protected boolean hasPermission()
    {
        return ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser());
    }

    public Collection getSchemes() throws GenericEntityException
    {
        return getSchemeManager().getSchemes();
    }

    public String[] getSchemeIds()
    {
        return schemeIds;
    }

    public void setSchemeIds(String[] schemeIds)
    {
        this.schemeIds = schemeIds;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public GenericValue getProject() throws GenericEntityException
    {
        if (project == null)
        {
            project = ManagerFactory.getProjectManager().getProject(getProjectId());
        }
        return project;
    }
}
