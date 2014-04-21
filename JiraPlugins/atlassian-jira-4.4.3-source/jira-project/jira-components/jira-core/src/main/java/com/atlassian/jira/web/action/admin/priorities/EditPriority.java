/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class EditPriority extends AbstractEditConstant
{
    private boolean preview = false;
    private String statusColor;

    public String doDefault() throws Exception
    {
        setStatusColor(getConstant().getString("statusColor"));
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!isPreview())
        {
            if (!TextUtils.stringSet(getIconurl()))
                addError("iconurl", getText("admin.errors.must.specify.url.for.icon"));

            if (!TextUtils.stringSet(getStatusColor()))
                addError("statusColor", getText("admin.errors.must.specify.color"));

            super.doValidation();
        }
    }

    protected String doExecute() throws Exception
    {
        if (isPreview())
        {
            return INPUT;
        }
        else
        {
            getConstant().set("statusColor", getStatusColor());
            return super.doExecute();
        }
    }

    protected String getConstantEntityName()
    {
        return "Priority";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.priority.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "priority";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getPriority(id);
    }

    protected String getRedirectPage()
    {
        return "ViewPriorities.jspa";
    }

    protected Collection getConstants()
    {
        return getConstantsManager().getPriorities();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshPriorities();
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
    }

    public boolean isPreview()
    {
        return preview;
    }

    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }
}
