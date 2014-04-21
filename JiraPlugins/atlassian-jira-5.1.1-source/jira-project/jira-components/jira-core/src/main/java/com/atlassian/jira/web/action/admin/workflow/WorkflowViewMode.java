package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.web.CookieNames;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
* Indicates which workflow mode the user is currently in.
*
* @since v5.1
*/
enum WorkflowViewMode
{
    TEXT("text"),
    DIAGRAM("diagram");

    private static final String COOKIE_NAME = "workflow-mode";
    private final String mode;

    WorkflowViewMode(String mode)
    {
        this.mode = mode;
    }

    String getCookieValue()
    {
        return mode;
    }

    static WorkflowViewMode parseFromAction(JiraWebActionSupport jwas)
    {
        final String value = jwas.getConglomerateCookieValue(CookieNames.AJS_CONGLOMERATE_COOKIE, COOKIE_NAME);
        for (WorkflowViewMode viewMode : WorkflowViewMode.values())
        {
            if (viewMode.getCookieValue().equals(value))
            {
                return viewMode;
            }
        }
        return DIAGRAM;
    }
}
