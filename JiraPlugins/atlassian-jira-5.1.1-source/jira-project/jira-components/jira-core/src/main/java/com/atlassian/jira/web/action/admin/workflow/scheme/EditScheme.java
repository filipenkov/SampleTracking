package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.scheme.AbstractEditScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class EditScheme extends AbstractEditScheme
{
    private final WorkflowSchemeManager workflowSchemeManager;

    public EditScheme(final WorkflowSchemeManager workflowSchemeManager)
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
