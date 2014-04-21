/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.project;

import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

public class AbstractProjectEntityCreate extends JiraNonWebActionSupport
{
    String name;
    GenericValue project;

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

    ///CLOVER:OFF
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public GenericValue getProject()
    {
        return project;
    }

    public void setProject(final GenericValue project)
    {
        this.project = project;
    }
}
