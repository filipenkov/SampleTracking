/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class EditIssueType extends AbstractEditConstant
{
    private final IssueTypeManager issueTypeManager;

    public EditIssueType(IssueTypeManager issueTypeManager)
    {
        this.issueTypeManager = issueTypeManager;
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getIconurl()))
        { addError("iconurl", getText("admin.errors.issuetypes.must.specify.url")); }

        super.doValidation();
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "type";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getIssueType(id);
    }

    protected String getRedirectPage()
    {
        return "ViewIssueTypes.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
    }

    @Override
    protected String createDuplicateMessage()
    {
        return getText("admin.errors.issue.type.with.this.name.already.exists");
    }

    @Override
    protected String doExecute() throws Exception
    {
        IssueType issueType = issueTypeManager.getIssueType(id);
        issueTypeManager.editIssueType(issueType, name, description, iconurl);
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect(getRedirectPage());
        }
    }
}
