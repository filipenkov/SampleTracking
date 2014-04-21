package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    private final WorkflowSchemeManager workflowSchemeManager;

    public DeleteScheme(final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public String getRedirectURL()
    {
        return "ViewWorkflowSchemes.jspa";
    }
}
