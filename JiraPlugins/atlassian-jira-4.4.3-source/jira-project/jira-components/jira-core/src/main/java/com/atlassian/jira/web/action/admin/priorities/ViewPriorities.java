/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class ViewPriorities extends AbstractViewConstants
{
    private boolean preview = false;
    private String statusColor;

    public ViewPriorities(final TranslationManager translationManager)
    {
        super(translationManager);
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

    public String doAddPriority() throws Exception
    {
        if (isPreview())
        {
            return INPUT;
        }
        else
        {
            if (!TextUtils.stringSet(getIconurl()))
                addError("iconurl", getText("admin.errors.must.specify.url.for.icon.of.priority"));

            if (!TextUtils.stringSet(getStatusColor()))
                addError("statusColor", getText("admin.errors.must.specify.color"));

            addField("statusColor", getStatusColor());

            return super.doAddConstant();
        }
    }

    protected String redirectToView()
    {
        return getRedirect("ViewPriorities.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY;
    }

    public boolean isPreview()
    {
        return preview;
    }

    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
    }
}
