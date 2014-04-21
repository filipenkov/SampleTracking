package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.DefaultJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugin.PluginParseException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Produces the velocity context for the workflow summary panel.
 *
 * @since v4.4
 */
public class WorkflowSummaryPanelContextProvider implements CacheableContextProvider
{
    private final ContextProviderUtils utils;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final JiraAuthenticationContext authenticationContext;
    private final TabUrlFactory factory;
    private final ComparatorFactory comparatorFactory;
    private final WorkflowManager workflowManager;

    public WorkflowSummaryPanelContextProvider(ContextProviderUtils utils, WorkflowSchemeManager workflowSchemeManager,
            JiraAuthenticationContext authenticationContext, TabUrlFactory factory, ComparatorFactory comparatorFactory,
            WorkflowManager workflowManager)
    {
        this.utils = utils;
        this.workflowSchemeManager = workflowSchemeManager;
        this.authenticationContext = authenticationContext;
        this.factory = factory;
        this.comparatorFactory = comparatorFactory;
        this.workflowManager = workflowManager;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
        //Nothing to do.
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        Project project = utils.getProject();
        MapBuilder<String, Object> builder = MapBuilder.newBuilder(context);
        addWorkflows(builder, project);
        addSchemeData(builder, project);
        return builder.toMap();
    }

    private MapBuilder<String, Object> addWorkflows(MapBuilder<String, Object> builder, Project project)
    {
        final Map<String, String> map = workflowSchemeManager.getWorkflowMap(project);

        String defaultWorkflow = map.get(null);
        if (defaultWorkflow == null)
        {
            defaultWorkflow = DefaultJiraWorkflow.DEFAULT_WORKFLOW_NAME;
        }

        Set<String> workflows = new HashSet<String>();
        for (String issueType : getIssueTypes(project))
        {
            String workflow = map.get(issueType);
            if (workflow == null)
            {
                workflow = defaultWorkflow;
            }
            workflows.add(workflow);
        }

        final List<SimpleWorkflow> ordered = new ArrayList<SimpleWorkflow>(workflows.size());
        for (String workflow : workflows)
        {
            final JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(workflow);

            ordered.add(new SimpleWorkflow(jiraWorkflow.getName(), getWorkflowUrl(workflow), jiraWorkflow.getDescription(), defaultWorkflow.equals(workflow)));
        }

        Collections.sort(ordered, comparatorFactory.createNamedDefaultComparator());

        return builder.add("workflows", ordered);
    }

    private MapBuilder<String, Object> addSchemeData(MapBuilder<String, Object> builder, Project project)
    {
        try
        {
            GenericValue workflowScheme = workflowSchemeManager.getWorkflowScheme(project.getGenericValue());
            if (workflowScheme != null)
            {
                builder.add("schemeName", workflowScheme.getString("name"));
                builder.add("schemeDescription", workflowScheme.getString("description"));
            }
            else
            {
                builder.add("schemeName", authenticationContext.getI18nHelper().getText("admin.schemes.workflows.default"));
            }
            builder.add("schemeLink", factory.forWorkflows());
        }
        catch (GenericEntityException e)
        {
            builder.add("error", true);
        }
        return builder;
    }

    String getWorkflowUrl(String workflowName)
    {
        return createUrlBuilder("/secure/admin/workflows/WorkflowDesigner.jspa?workflowMode=live")
                .addParameter("wfName", workflowName).asUrlString();
    }

    private Set<String> getIssueTypes(Project project)
    {
        Set<String> issueTypes = new HashSet<String>();
        for (IssueType type : project.getIssueTypes())
        {
            issueTypes.add(type.getId());
        }
        return issueTypes;
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return utils.createUrlBuilder(operation);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleWorkflow implements NamedDefault
    {
        private final String name;
        private final String urlName;
        private final String description;
        private final boolean isDefault;

        SimpleWorkflow(final String name, final String urlName, final String description, final boolean isDefault)
        {
            this.name = name;
            this.urlName = urlName;
            this.description = description;
            this.isDefault = isDefault;
        }

        public String getName()
        {
            return name;
        }

        public String getUrl()
        {
            return urlName;
        }

        public String getDescription()
        {
            return description;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflow that = (SimpleWorkflow) o;

            if (isDefault != that.isDefault) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null)
            {
                return false;
            }
            if (!name.equals(that.name)) { return false; }
            if (urlName != null ? !urlName.equals(that.urlName) : that.urlName != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name.hashCode();
            result = 31 * result + (urlName != null ? urlName.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (isDefault ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "SimpleWorkflow{" +
                    "name='" + name + '\'' +
                    ", urlName='" + urlName + '\'' +
                    ", description='" + description + '\'' +
                    ", isDefault=" + isDefault +
                    '}';
        }
    }
}
