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
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.Collection;
import java.util.Iterator;

@WebSudoRequired
public class EditProjectCategory extends ProjectActionSupport
{
    private Long id = null;
    private String name = null;
    private String description = null;

    protected void doValidation()
    {
        // Valid name must have some content.
        if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.please.specify.a.name"));
        }

        // Confirm that the project category id actually maps to a project category.
        if (null == getId() || null == ManagerFactory.getProjectManager().getProjectCategory(getId()))
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else
        {
            // Validate that the name is not the name of another project category
            Collection projectCategories = ManagerFactory.getProjectManager().getProjectCategories();
            for (Iterator iter = projectCategories.iterator(); iter.hasNext();)
            {
                GenericValue projectCategory = (GenericValue) iter.next();

                //cannot have two categories with a different id and the same name
                if (!getId().equals(projectCategory.getLong("id")) && TextUtils.noNull(getName()).equalsIgnoreCase(projectCategory.getString("name")))
                {
                    addError("name", getText("admin.errors.project.category.already.exists","'" + getName() + "'"));
                    break;
                }
            }
        }
    }

    /**
     * Populate name and description fields given a project category id.
     * @throws Exception
     */
    public String doDefault() throws Exception
    {
        if (null == getId() || null == ManagerFactory.getProjectManager().getProjectCategory(getId()))
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else
        {
            GenericValue projectCategory = ManagerFactory.getProjectManager().getProjectCategory(getId());

            setName(projectCategory.getString("name"));
            setDescription(projectCategory.getString("description"));
        }

        return INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.PROJECTCATEGORY_EDIT, EasyMap.build("id", getId(), "name", getName(), "description", TextUtils.noNull(getDescription())));
        ActionUtils.checkForErrors(aResult);

        return getRedirect("ViewProjectCategories!default.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
