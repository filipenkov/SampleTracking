/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.project;

import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.ofbiz.core.entity.GenericValue;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

/**
 * This class is a common class for the editing of project entities. The classes used to edit individual entities
 * (components, versions etc) should extend this class
 */
public class AbstractProjectEntityEdit extends JiraNonWebActionSupport
{
    private String name;
    private String description;
    private GenericValue project;
    private GenericValue entity;

    /**
     * Carries out validation that the name must exist and a project be selected. if not then an ErrorMessage will be
     * added to the webwork action
     */
    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(getName()))
        {
            addErrorMessage(getText("admin.errors.must.specify.entity.name"));
        }

        if (getProject() == null)
        {
            addErrorMessage(getText("admin.errors.must.specify.project"));
        }
    }

    /**
     * Sets the name of the entity to the new name
     *
     * @return String to indicate success of action
     */
    @Override
    protected String doExecute() throws Exception
    {
        getEntity().setString("name", getName());

        getEntity().store();

        return getResult();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public GenericValue getProject()
    {
        return project;
    }

    public void setProject(final GenericValue project)
    {
        this.project = project;
    }

    public GenericValue getEntity()
    {
        return entity;
    }

    public void setEntity(final GenericValue entity)
    {
        this.entity = entity;
    }
}
