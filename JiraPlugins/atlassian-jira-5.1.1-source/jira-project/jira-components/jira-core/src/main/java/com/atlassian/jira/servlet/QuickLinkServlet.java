package com.atlassian.jira.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.apache.xml.security.utils.I18n;
import org.ofbiz.core.entity.GenericEntityException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class QuickLinkServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(QuickLinkServlet.class);

    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        UtilTimerStack.push("QuickLinkServlet.service()");
        try
        {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("") || pathInfo.equals("/"))
            {
                RequestDispatcher rd = request.getRequestDispatcher("/secure/BrowseProject.jspa");
                rd.forward(request, response);
                return; // Always return to avoid getting double send errors
            }
            else if (JiraKeyUtils.validIssueKey(pathInfo.substring(1).toUpperCase()))
            {
                if (linkToIssue(request, response, pathInfo, true))
                {
                    return; // Always return to avoid getting double send errors
                }
            }
            else
            {
                BrowseProjectUrlHelper helper = new BrowseProjectUrlHelper(pathInfo);

                // This is a project
                String projectKey = helper.getProjectKey();
                Project project = null;

                //attempt to resolve the project from the capitalised project key
                if (projectKey != null)
                {
                    projectKey = projectKey.toUpperCase();
                    project = getProjectManager().getProjectObjByKey(projectKey);

                    // If projectKey does not specify a valid project, check for a trailing slash and if there is
                    // remove it & try resolving the project again
                    if (project == null && projectKey.endsWith("/"))
                    {
                        projectKey = projectKey.substring(0, projectKey.length() - 1);
                        project = getProjectManager().getProjectObjByKey(projectKey);
                    }
                }

                if (project != null)
                {
                    if (helper.getVersionId() != null)
                    {
                        RequestDispatcher rd = request.getRequestDispatcher("/secure/BrowseVersion.jspa?id=" + project.getId() + "&versionId=" + helper.getVersionId());
                        rd.forward(request, response);
                        return; // Always return to avoid getting double send errors
                    }
                    else if (helper.getComponentId() != null)
                    {
                        RequestDispatcher rd = request.getRequestDispatcher("/secure/BrowseComponent.jspa?id=" + project.getId() + "&componentId=" + helper.getComponentId());
                        rd.forward(request, response);
                        return; // Always return to avoid getting double send errors
                    }
                    else if (helper.getProjectAvatarId() != null)
                    {
                        String avatarUrl = "/secure/projectavatar?pid=" + project.getId();
                        if (helper.getProjectAvatarId() != BrowseProjectUrlHelper.CURRENT_PROJECT_AVATAR)
                        {
                            avatarUrl += "&avatarId=" + helper.getProjectAvatarId();
                        }
                        RequestDispatcher rd = request.getRequestDispatcher(avatarUrl);
                        rd.forward(request, response);
                        return;
                    }
                    else
                    {
                        RequestDispatcher rd = request.getRequestDispatcher("/secure/BrowseProject.jspa?id=" + project.getId());
                        rd.forward(request, response);
                        return; // Always return to avoid getting double send errors
                    }
                }
                else
                {
                    // JRA-15856:  If the project regex has changed we might not have recognised the url as a valid issue key,
                    // so try again as a last resort
                    if (linkToIssue(request, response, pathInfo, false))
                    {
                        return; // Always return to avoid getting double send errors
                    }
                    // forward to issue does not exist page
                    JiraContactHelper jiraContactHelper = getJiraContactHelper();
                    String contactAdministratorLinkHtml = jiraContactHelper.getAdministratorContactLinkHtml(request.getContextPath(), getI18Helper());
                    request.setAttribute("administratorContactLink", contactAdministratorLinkHtml);

                    RequestDispatcher rd = request.getRequestDispatcher("/secure/views/projectnotfound.jsp");
                    response.setStatus(404);
                    rd.forward(request, response);
                    return; // Always return to avoid getting double send errors
                }
            }
        }
        finally
        {
            UtilTimerStack.pop("QuickLinkServlet.service()");
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings (value = "HRS_REQUEST_PARAMETER_TO_HTTP_HEADER", justification = "JIRA has a HeaderSanitisingFilter that protects against this")
    private boolean linkToIssue(final HttpServletRequest request, final HttpServletResponse response, final String pathInfo, final boolean redirectAlways)
            throws IOException, ServletException
    {
        String key = pathInfo.substring(1, pathInfo.length()).toUpperCase();
        try
        {
            Issue issue = getIssueManager().getIssueObject(key);
            if (issue == null)
            {
                issue = getChangeHistoryManager().findMovedIssue(key);
                if (issue != null)
                {
                    String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
                    String queryString = request.getQueryString() != null ? '?' + request.getQueryString() : "";
                    response.sendRedirect(contextPath + "/browse/" + issue.getKey() + queryString);
                    return true;
                }
            }
            if (issue != null || redirectAlways)
            {
                // we co-operate with our action so they dont have to re-read what we just read.
                request.setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT, issue);

                String queryString = request.getQueryString() != null ? "&" + request.getQueryString() : "";
                RequestDispatcher rd = request.getRequestDispatcher("/secure/ViewIssue.jspa?key=" + key + queryString);
                rd.forward(request, response);
                return true;
            }
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
        }
        return false;
    }

    ProjectManager getProjectManager()
    {
        return ComponentAccessor.getProjectManager();
    }

    IssueManager getIssueManager()
    {
        return ComponentAccessor.getIssueManager();
    }

    ChangeHistoryManager getChangeHistoryManager()
    {
        return ComponentAccessor.getChangeHistoryManager();
    }

    JiraContactHelper getJiraContactHelper()
    {
        return ComponentAccessor.getComponent(JiraContactHelper.class);
    }

    User getLoggedInUser()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
    }

    I18nHelper getI18Helper()
    {
        return ComponentAccessor.getI18nHelperFactory().getInstance(getLoggedInUser());
    }

}
