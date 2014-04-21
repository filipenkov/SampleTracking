package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class DeleteWorkflowSchemeEntity extends SchemeAwareWorkflowAction
{
    private Long id;
    private boolean confirmed = false;
    private final WorkflowManager workflowManager;

    public DeleteWorkflowSchemeEntity(final WorkflowManager workflowManager)
    {
        this.workflowManager = workflowManager;
    }

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
            return returnComplete("ViewWorkflowSchemes.jspa");
        else
            return returnCompleteWithInlineRedirect("EditWorkflowSchemeEntities!default.jspa?schemeId=" + getSchemeId());
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
        return workflowManager.getWorkflow(getWorkflowSchemeEntity().getString("workflow"));
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
