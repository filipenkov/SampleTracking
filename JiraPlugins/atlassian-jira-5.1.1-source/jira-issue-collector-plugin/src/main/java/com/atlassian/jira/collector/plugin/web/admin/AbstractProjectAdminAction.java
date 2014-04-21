package com.atlassian.jira.collector.plugin.web.admin;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.security.Permissions.PROJECT_ADMIN;

public class AbstractProjectAdminAction extends JiraWebActionSupport
{
    private String projectKey;
    private Project project;

    /**
     * This is an ugly hack to ensure that the header will render correctly in the project admin section.
     * For this to work the project has to be set on the request as an attribute.
     * See https://jira.atlassian.com/browse/JRA-26407
     */
    protected void initRequest()
    {
        final HttpServletRequest request = ExecutingHttpRequest.get();
        request.setAttribute(ServletRequestProjectConfigRequestCache.class.getName() + ":project", getProject());
    }

    public Project getProject()
    {
        if (project == null)
        {
            project = getProjectManager().getProjectObjByKey(projectKey);
        }
        return project;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    final protected boolean isProjectAdmin(Project project)
    {
        return getPermissionManager().hasPermission(PROJECT_ADMIN, project, getLoggedInUser());
    }
}
