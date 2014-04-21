/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.Collection;

@WebSudoRequired
public class DeleteProjectCategory extends ProjectActionSupport
{
    private boolean confirm = false;
    private Long id;

    private final CustomFieldManager customFieldManager;

    public DeleteProjectCategory(ProjectManager projectManager, PermissionManager permissionManager, CustomFieldManager customFieldManager)
    {
        super(projectManager, permissionManager);
        this.customFieldManager = customFieldManager;
    }

    protected void doValidation()
    {
        // Deletion must be confirmed
        if (!isConfirm())
        {
            addErrorMessage(getText("admin.errors.projectcategory.must.confirm.delete"));
        }

        try
        {
            // Must specify which project category to delete
            if (null == getId() || null == ManagerFactory.getProjectManager().getProjectCategory(getId()))
            {
                addErrorMessage(getText("admin.errors.projectcategory.must.specify.category"));
            }
            else
            {
                // Confirm that there are no linked projects to this project category.
                final Collection projectsFromProjectCategory = getProjects();
                if (null != projectsFromProjectCategory && !projectsFromProjectCategory.isEmpty())
                {
                    addErrorMessage(getText("admin.errors.projectcategory.currently.projects.linked"));
                }
            }
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.projectcategory.must.specify.category"));
        }
    }

    private Collection getProjects() throws GenericEntityException
    {
        return ManagerFactory.getProjectManager().getProjectsFromProjectCategory(getProjectCategory());
    }

    private GenericValue getProjectCategory() throws GenericEntityException
    {
        return ManagerFactory.getProjectManager().getProjectCategory(getId());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        customFieldManager.removeProjectCategoryAssociations(getProjectCategory());

        ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.PROJECTCATEGORY_DELETE, EasyMap.build("id", getId()));
        ActionUtils.checkForErrors(aResult);

        return getRedirect("ViewProjectCategories!default.jspa");
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
