/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class DeleteWorkflowSchemeEntity extends SchemeAwareWorkflowAction
{
    private Long id;
    private boolean confirmed = false;

    protected void doValidation()
    {
        if (id == null)
            addErrorMessage(getText("admin.errors.workflows.specify.scheme.entity"));
        if (!confirmed)
            addErrorMessage(getText("admin.errors.workflows.confirm.deletion"));
    }

    protected String doExecute() throws Exception
    {
        getSchemeManager().deleteEntity(getId());
        if (getSchemeId() == null)
            return getRedirect("ViewWorkflowSchemes.jspa");
        else
            return getRedirect("EditWorkflowSchemeEntities!default.jspa?schemeId=" + getSchemeId());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public GenericValue getWorkflowSchemeEntity() throws GenericEntityException
    {
        return getSchemeManager().getEntity(id);
    }

    public JiraWorkflow getWorkflow() throws GenericEntityException
    {
        return ManagerFactory.getWorkflowManager().getWorkflow(getWorkflowSchemeEntity().getString("workflow"));
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public String getRedirectURL()
    {
        return null;
    }
}
