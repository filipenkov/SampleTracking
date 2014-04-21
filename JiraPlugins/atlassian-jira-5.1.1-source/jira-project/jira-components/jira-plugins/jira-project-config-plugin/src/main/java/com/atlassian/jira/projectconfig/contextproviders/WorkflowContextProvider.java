package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context provider for the workflow tab.
 *
 * @since v4.4
 */

public class WorkflowContextProvider implements CacheableContextProvider
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ContextProviderUtils contextProviderUtils;
    private final WorkflowManager workflowManager;
    private final ProjectWorkflowSchemeHelper helper;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final ComparatorFactory comparatorFactory;

    public WorkflowContextProvider(WorkflowSchemeManager workflowSchemeManager, ContextProviderUtils contextProviderUtils,
            WorkflowManager workflowManager, ProjectWorkflowSchemeHelper helper, IssueTypeSchemeManager issueTypeSchemeManager,
            ComparatorFactory comparatorFactory)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.contextProviderUtils = contextProviderUtils;
        this.workflowManager = workflowManager;
        this.helper = helper;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.comparatorFactory = comparatorFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        //Nothing to do.
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Project project = contextProviderUtils.getProject();
        MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        addWorkflowData(contextBuilder, project);
        addSchemeData(contextBuilder, project);

        Map<String, Object> defaults = CompositeMap.of(context, contextProviderUtils.getDefaultContext());
        return CompositeMap.of(contextBuilder.toMap(), defaults);
    }

    MapBuilder<String, Object> addSchemeData(MapBuilder<String, Object> builder, Project project)
    {
        try
        {
            GenericValue workflowScheme = workflowSchemeManager.getWorkflowScheme(project.getGenericValue());
            if (workflowScheme != null)
            {
                builder.add("workflowScheme", new SimpleWorkflowScheme(workflowScheme));
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        return builder;
    }

    MapBuilder<String, Object> addWorkflowData(MapBuilder<String, Object> builder, Project project)
    {
        final Comparator<String> collator = contextProviderUtils.getStringComparator();
        final Map<String, String> workflowMap = workflowSchemeManager.getWorkflowMap(project);
        String defaultWorkflow = workflowMap.get(null);
        if (defaultWorkflow == null)
        {
            defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
        }

        final Multimap<String, IssueType> invertedWorkflowMap = HashMultimap.create();
        for (final IssueType type : project.getIssueTypes())
        {
            String workflowName = workflowMap.get(type.getId());
            if (workflowName == null)
            {
                workflowName = defaultWorkflow;
            }
            invertedWorkflowMap.put(workflowName, type);
        }

        final Multimap<String, Project> projectMap = getWorkflowMap(invertedWorkflowMap.keySet());
        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());
        final List<SimpleWorkflowData> result = new ArrayList<SimpleWorkflowData>();
        for (final Map.Entry<String, Collection<IssueType>> entry : invertedWorkflowMap.asMap().entrySet())
        {
            final String name = entry.getKey();
            final JiraWorkflow workflow = workflowManager.getWorkflow(name);

            List<SimpleIssueType> issueTypes = createIssueTypes(entry.getValue(), defaultIssueType);
            List<SimpleProject> projectsUsingWorkflow = createProjects(projectMap.get(name), collator);
            SimpleWorkflow simpleWorkflow = createWorkflow(workflow, defaultWorkflow.equals(name));
            result.add(new SimpleWorkflowData(issueTypes, projectsUsingWorkflow, simpleWorkflow));
        }

        final Comparator<NamedDefault> namedComparator = comparatorFactory.createNamedDefaultComparator();
        Collections.sort(result, new Comparator<SimpleWorkflowData>()
        {
            @Override
            public int compare(SimpleWorkflowData o1, SimpleWorkflowData o2)
            {
                return namedComparator.compare(o1.getWorkflow(), o2.getWorkflow());
            }
        });

        return builder.add("workflows", result);
    }

    private Multimap<String, Project> getWorkflowMap(Set<String> allowedWorkflows)
    {
        return helper.getProjectsForWorkflow(allowedWorkflows);
    }

    private List<SimpleIssueType> createIssueTypes(final Collection<? extends IssueType> value, IssueType defaultIssueType)
    {
        final List<SimpleIssueType> types = new ArrayList<SimpleIssueType>();
        for (IssueType issueType : value)
        {
            types.add(createIssueType(issueType, defaultIssueType != null && issueType.equals(defaultIssueType)));
        }
        Collections.sort(types, comparatorFactory.createIssueTypeComparator());

        return types;
    }

    private List<SimpleProject> createProjects(final Collection<? extends Project> projects, final Comparator<String> order)
    {
        if (projects == null || projects.isEmpty())
        {
            return Collections.emptyList();
        }

        List<SimpleProject> simpleProjects = new ArrayList<SimpleProject>();
        for (Project project : projects)
        {
            simpleProjects.add(new SimpleProject(project));
        }
        Collections.sort(simpleProjects, new Comparator<SimpleProject>()
        {
            @Override
            public int compare(SimpleProject o1, SimpleProject o2)
            {
                return order.compare(o1.getName(), o2.getName());
            }
        });
        return simpleProjects;
    }

    SimpleWorkflow createWorkflow(JiraWorkflow workflow, boolean defaultWf)
    {
        final WorkflowActionsBean bean = getActionsBean();
        final WorkflowDescriptor descriptor = workflow.getDescriptor();
        @SuppressWarnings ( { "unchecked" }) final List<StepDescriptor> steps = descriptor.getSteps();
        final List<SimpleWorkflowSource> sources = new ArrayList<SimpleWorkflowSource>(steps.size());
        for (StepDescriptor step : steps)
        {
            @SuppressWarnings ( { "unchecked" }) final List<ActionDescriptor> actions = step.getActions();
            final List<SimpleWorkflowTarget> targets = new ArrayList<SimpleWorkflowTarget>(actions.size());
            final Status sourceStatus = workflow.getLinkedStatusObject(step);

            for (ActionDescriptor action : actions)
            {
                final int targetStepId = action.getUnconditionalResult().getStep();
                final Status targetStatus;
                if (targetStepId == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
                {
                    targetStatus = sourceStatus;
                }
                else
                {
                    targetStatus = workflow.getLinkedStatusObject(descriptor.getStep(targetStepId));
                }

                final FieldScreen transitionScreen = bean.getFieldScreenForView(action);

                targets.add(new SimpleWorkflowTarget(targetStatus, action.getName(), transitionScreen));
            }
            sources.add(new SimpleWorkflowSource(sourceStatus, targets));
        }

        return new SimpleWorkflow(descriptor.getEntityId(), workflow.getName(), workflow.getDescription(), sources, defaultWf, workflow.isSystemWorkflow());
    }

    WorkflowActionsBean getActionsBean()
    {
        return new WorkflowActionsBean();
    }

    private SimpleIssueType createIssueType(IssueType type, boolean isDefault)
    {
        return new SimpleIssueTypeImpl(type, isDefault);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleWorkflowData
    {
        private final List<SimpleProject> projects;
        private final SimpleWorkflow workflow;
        private final List<SimpleIssueType> issueTypes;

        public SimpleWorkflowData(List<SimpleIssueType> issueTypes, List<SimpleProject> projects, SimpleWorkflow workflow)
        {
            this.issueTypes = issueTypes;
            this.projects = projects;
            this.workflow = workflow;
        }

        public List<SimpleIssueType> getIssueTypes()
        {
            return issueTypes;
        }

        public List<SimpleProject> getProjects()
        {
            return projects;
        }

        public SimpleWorkflow getWorkflow()
        {
            return workflow;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflowData that = (SimpleWorkflowData) o;

            if (issueTypes != null ? !issueTypes.equals(that.issueTypes) : that.issueTypes != null) { return false; }
            if (projects != null ? !projects.equals(that.projects) : that.projects != null) { return false; }
            if (workflow != null ? !workflow.equals(that.workflow) : that.workflow != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = projects != null ? projects.hashCode() : 0;
            result = 31 * result + (workflow != null ? workflow.hashCode() : 0);
            result = 31 * result + (issueTypes != null ? issueTypes.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class SimpleWorkflow implements Iterable<SimpleWorkflowSource>, NamedDefault
    {
        private final String name;
        private final String description;
        private final boolean defaultWorkflow;
        private final List<SimpleWorkflowSource> steps;
        private final boolean systemWorkflow;
        private final int id;

        SimpleWorkflow(int id, String name, String description, List<SimpleWorkflowSource> steps, boolean defaultWorkflow, boolean isSystemWorkflow)
        {
            this.id = id;
            this.description = description;
            this.defaultWorkflow = defaultWorkflow;
            this.name = name;
            this.steps = steps;
            systemWorkflow = isSystemWorkflow;
        }

        public boolean isDefaultWorkflow()
        {
            return defaultWorkflow;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public boolean isDefault()
        {
            return isDefaultWorkflow();
        }

        public List<SimpleWorkflowSource> getSources()
        {
            return steps;
        }

        @Override
        public Iterator<SimpleWorkflowSource> iterator()
        {
            return steps.iterator();
        }

        public boolean isSystemWorkflow()
        {
            return systemWorkflow;
        }

        public int getId()
        {
            return id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflow that = (SimpleWorkflow) o;

            if (defaultWorkflow != that.defaultWorkflow) { return false; }
            if (id != that.id) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
            if (steps != null ? !steps.equals(that.steps) : that.steps != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (defaultWorkflow ? 1 : 0);
            result = 31 * result + (steps != null ? steps.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + id;
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class SimpleWorkflowSource implements Iterable<SimpleWorkflowTarget>
    {
        private final Status fromStatus;
        private final List<SimpleWorkflowTarget> targets;

        public SimpleWorkflowSource(Status fromStatus, List<SimpleWorkflowTarget> targets)
        {
            this.fromStatus = fromStatus;
            this.targets = targets;
        }

        public Status getFromStatus()
        {
            return fromStatus;
        }

        public List<SimpleWorkflowTarget> getTargets()
        {
            return targets;
        }

        @Override
        public Iterator<SimpleWorkflowTarget> iterator()
        {
            return targets.iterator();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflowSource that = (SimpleWorkflowSource) o;

            if (fromStatus != null ? !fromStatus.equals(that.fromStatus) : that.fromStatus != null) { return false; }
            if (targets != null ? !targets.equals(that.targets) : that.targets != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = fromStatus != null ? fromStatus.hashCode() : 0;
            result = 31 * result + (targets != null ? targets.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class SimpleWorkflowTarget
    {
        private final Status toStatus;
        private final String transitionName;
        private final FieldScreen screen;

        public SimpleWorkflowTarget(Status toStatus, String transitionName, FieldScreen screen)
        {
            this.screen = screen;
            this.toStatus = toStatus;
            this.transitionName = transitionName;
        }

        public FieldScreen getScreen()
        {
            return screen;
        }

        public Status getToStatus()
        {
            return toStatus;
        }

        public String getTransitionName()
        {
            return transitionName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflowTarget that = (SimpleWorkflowTarget) o;

            if (screen != null ? !screen.equals(that.screen) : that.screen != null) { return false; }
            if (toStatus != null ? !toStatus.equals(that.toStatus) : that.toStatus != null) { return false; }
            if (transitionName != null ? !transitionName.equals(that.transitionName) : that.transitionName != null)
            { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = toStatus != null ? toStatus.hashCode() : 0;
            result = 31 * result + (transitionName != null ? transitionName.hashCode() : 0);
            result = 31 * result + (screen != null ? screen.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class SimpleWorkflowScheme
    {
        private final String name;
        private final String description;
        private final Long id;

        private SimpleWorkflowScheme(GenericValue value)
        {
            this.description = value.getString("description");
            this.id = value.getLong("id");
            this.name = value.getString("name");
        }

        SimpleWorkflowScheme(Long id, String name, String description)
        {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflowScheme that = (SimpleWorkflowScheme) o;

            if (description != null ? !description.equals(that.description) : that.description != null)
            {
                return false;
            }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
