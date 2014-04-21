/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class DeletePriority extends AbstractDeleteConstant
{
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

    protected void postProcess(String id)
    {
        if (id.equals(getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY)))
        {
            getApplicationProperties().setString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY, null);
        }
    }

}
