/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class EditStatus extends AbstractEditConstant
{
    private final StatusManager statusManager;

    public EditStatus(StatusManager statusManager)
    {
        this.statusManager = statusManager;
    }

    protected String getConstantEntityName()
    {
        return "Status";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.status.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "status";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getStatus(id);
    }

    protected String getRedirectPage()
    {
        return "ViewStatuses.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getStatuses();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        Status status = statusManager.getStatus(id);
        statusManager.editStatus(status, name, description, iconurl);
        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect(getRedirectPage());
    }


}
