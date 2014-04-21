package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AbstractFieldScreenSchemeItemAction extends AbstractFieldScreenSchemeAction
{
    private final FieldScreenManager fieldScreenManager;
    private Long issueOperationId;
    private Long fieldScreenId;
    private Collection fieldScreens;

    public AbstractFieldScreenSchemeItemAction(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenSchemeManager);
        this.fieldScreenManager = fieldScreenManager;
    }

    protected String redirectToView()
    {
        return getRedirect("ConfigureFieldScreenScheme.jspa?id=" + getId());
    }

    protected void validateIssueOperationId()
    {
        // Null operation id represents the default entry
        if (issueOperationId != null && IssueOperations.getIssueOperation(issueOperationId) == null)
        {
            addError("issueOperationId", getText("admin.errors.screens.invalid.issue.operation.id"));
        }
    }

    protected void validateFieldScreenId()
    {
        if (getFieldScreenId() == null)
        {
            addError("fieldScreenId", getText("admin.errors.screens.please.select.screen"));
        }
        else if (getFieldScreenManager().getFieldScreen(getFieldScreenId()) == null)
        {
            addError("fieldScreenId", getText("admin.errors.screens.invalid.id"));
        }
    }

    public Long getIssueOperationId()
    {
        return issueOperationId;
    }

    public void setIssueOperationId(Long issueOperationId)
    {
        this.issueOperationId = issueOperationId;
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }

    public void setFieldScreenId(Long fieldScreenId)
    {
        this.fieldScreenId = fieldScreenId;
    }

    public Collection getFieldScreens()
    {
        if (fieldScreens == null)
        {
            fieldScreens = fieldScreenManager.getFieldScreens();
        }

        return fieldScreens;
    }

    protected FieldScreenManager getFieldScreenManager()
    {
        return fieldScreenManager;
    }

    public IssueOperation getIssueOperation()
    {
        if (getIssueOperationId() != null)
            return IssueOperations.getIssueOperation(getIssueOperationId());
        else
            return null;
    }
}
