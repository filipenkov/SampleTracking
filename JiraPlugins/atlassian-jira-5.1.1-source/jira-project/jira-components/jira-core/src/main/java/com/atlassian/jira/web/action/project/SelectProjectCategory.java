/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;

@WebSudoRequired
public class SelectProjectCategory extends JiraWebActionSupport
{
    private Long pid;
    private Long pcid;
    private final ProjectManager projectManager;

    public SelectProjectCategory(ProjectManager projectManager) 
    {
        this.projectManager = projectManager;
    }

    public String doDefault() throws Exception
    {
        ProjectCategory projectCategory = projectManager.getProjectCategoryForProject(getProject());

        if (null != projectCategory)
            setPcid(projectCategory.getId());
        else
            setPcid(new Long(-1));

        return super.doDefault();
    }

    protected void doValidation()
    {
        // Must have a valid project
        if (null == getProject())
        {
            addErrorMessage(getText("admin.errors.project.specify.project"));
        }

        // Either a valid project category, or null
        if (!new Long(-1).equals(getPcid()) && null == getProjectCategory())
        {
            addError("pcid", getText("admin.errors.project.specify.project.category"));
        }
    }

    /**
     * Given a project, remove all project category links, then create one if supplied a project category.
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        projectManager.setProjectCategory(getProject(), getProjectCategory());

        return getRedirect("/plugins/servlet/project-config/" + getProject().getKey() + "/summary");
    }

    public Collection getProjectCategories() throws GenericEntityException
    {
        return projectManager.getProjectCategories();
    }

    public Project getProject()
    {
        return projectManager.getProjectObj(getPid());
    }

    private ProjectCategory getProjectCategory()
    {
        if (null == getPcid() || getPcid().equals(new Long(-1)))
            return null;

        return projectManager.getProjectCategoryObject(getPcid());
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public Long getPcid()
    {
        return pcid;
    }

    public void setPcid(Long pcid)
    {
        this.pcid = pcid;
    }
}
