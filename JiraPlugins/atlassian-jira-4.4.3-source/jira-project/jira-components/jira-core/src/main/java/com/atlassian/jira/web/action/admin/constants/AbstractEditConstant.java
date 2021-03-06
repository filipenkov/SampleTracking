/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractEditConstant extends AbstractConstantAction
{
    String id;
    GenericValue constant;
    String name;
    String description;
    String iconurl;

    public String doDefault() throws Exception
    {
        name = getConstant().getString("name");
        description = getConstant().getString("description");
        iconurl = getConstant().getString("iconurl");

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (getConstant() == null)
        {
            addErrorMessage(getText("admin.errors.specified.constant.does.not.exist"));
        }

        String actualName = StringUtils.trimToNull(name);
        if (actualName == null)
            addError("name", getText("admin.errors.must.specify.name"));

        // Check if the name is the same as an existing constant
        IssueConstant constantByName = getConstantsManager().getConstantByNameIgnoreCase(getConstantEntityName(), actualName);
        if (constantByName != null && !constantByName.getId().equals(getConstant().getString("id")))
        {
            addError("name", createDuplicateMessage());
        }
    }

    protected String createDuplicateMessage()
    {
        return getText("admin.errors.constant.already.exists", getNiceConstantName());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getConstant().set("name", name);
        getConstant().set("description", description);
        getConstant().set("iconurl", iconurl);

        getConstant().store();

        getConstantsManager().refresh();
        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect(getRedirectPage());
    }

    public GenericValue getConstant()
    {
        if (constant == null)
        {
            constant = getConstant(id);
        }

        return constant;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }
}
