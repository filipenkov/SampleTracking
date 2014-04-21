package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class JiraHelper
{
    private final HttpServletRequest request;
    private final Project project;
    private final Map<String, Object> params;

    public JiraHelper()
    {
        this(null);
    }

    public JiraHelper(final HttpServletRequest request)
    {
        this(request, (Project) null);
    }

    public JiraHelper(final HttpServletRequest request, final Project project)
    {
        this(request, project, new HashMap<String, Object>());
    }

    public JiraHelper(final HttpServletRequest request, final Project project, final Map<String, Object> params)
    {
        this.request = request;
        this.project = project;
        this.params = params;
    }

    /**
     * Constructs an instance of this class.
     *
     * @param request HTTP Servlet Request
     * @param project project generic value
     * @deprecated please use {@link #JiraHelper(javax.servlet.http.HttpServletRequest,com.atlassian.jira.project.Project)} instead
     */
    @Deprecated
    public JiraHelper(final HttpServletRequest request, final GenericValue project)
    {
        this.request = request;
        this.project = project == null ? null : getProjectManager().getProjectObj(project.getLong("id"));
        params = new HashMap<String, Object>();
    }

    ///CLOVER:OFF
    /**
     * Returns project manager.
     * <p/>
     * NOTE: This method is here for unit testing purposes only.
     *
     * @return project manager
     */
    ProjectManager getProjectManager()
    {
        return ComponentAccessor.getProjectManager();
    }

    ///CLOVER:ON

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public Project getProjectObject()
    {
        return project;
    }

    /**
     * Returns project generic value
     *
     * @return project generic value
     * @deprecated please use {@link #getProjectObject()} instead
     */
    @Deprecated
    public GenericValue getProject()
    {
        return project == null ? null : project.getGenericValue();
    }

    /**
     * Returns the query string to represent this helper.
     *
     * TODO: replace {@link #project} with a {@link com.atlassian.jira.project.browse.BrowseContext} so that we can just call BrowseContext#getQueryString() instead.
     *
     * @return
     */
    public String getQueryString()
    {
        if ((project != null) && (project.getId() != null))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("&amp;");
            sb.append(SystemSearchConstants.forProject().getUrlParameter());
            sb.append("=");
            sb.append(project.getId());
            return sb.toString();
        }
        return "";
    }

    public Map<String, Object> getContextParams()
    {
        final Map<String, Object> newParams = MapBuilder.<String, Object> newBuilder().add("project", project).add("request", request).toMutableMap();
        return CompositeMap.of(newParams, params);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        JiraHelper that = (JiraHelper) o;

        if (params != null ? !params.equals(that.params) : that.params != null) { return false; }
        if (project != null ? !project.equals(that.project) : that.project != null) { return false; }
        if (request != null ? !request.equals(that.request) : that.request != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        return result;
    }
}
