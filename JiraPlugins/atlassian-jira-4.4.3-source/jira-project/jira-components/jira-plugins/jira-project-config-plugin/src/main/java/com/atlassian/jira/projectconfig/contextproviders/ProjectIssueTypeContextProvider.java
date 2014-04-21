package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the issuetypes panel
 *
 * @since v4.4
 */
public class ProjectIssueTypeContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_ISSUE_TYPES_KEY = "issueTypes";
    static final String CONTEXT_ISSUE_TYPE_SCHEME_KEY = "issueTypeScheme";
    static final String CONTEXT_ERRORS_KEY = "errors";

    static final String ISSUE_TYPE_SCHEME_ERROR_I18N_KEY = "admin.project.config.summary.issuetypes.no.issuetypescheme.error";

    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final WorkflowSchemeManager worlflowSchemeManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final ComparatorFactory comparatorFactory;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ContextProviderUtils contextProviderUtils;

    public ProjectIssueTypeContextProvider(final IssueTypeSchemeManager issueTypeSchemeManager,
            ContextProviderUtils contextProviderUtils, WorkflowSchemeManager worlflowSchemeManager,
            IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldLayoutManager fieldLayoutManager,
            ComparatorFactory comparatorFactory)
    {
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.contextProviderUtils = contextProviderUtils;
        this.worlflowSchemeManager = worlflowSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final List<String> errors = Lists.newArrayList();

        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);
        final I18nHelper i18nHelper = (I18nHelper) defaultContext.get(ContextProviderUtils.CONTEXT_I18N_KEY);
        final Collection<ProjectIssueType> simpleIssueTypes = getSimpleIssueTypes(project);
        final FieldConfigScheme issueTypeScheme = issueTypeSchemeManager.getConfigScheme(project);

        if(issueTypeScheme == null)
        {
            errors.add(i18nHelper.getText(ISSUE_TYPE_SCHEME_ERROR_I18N_KEY));
        }
        contextMap.add(CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes);
        contextMap.add(CONTEXT_ISSUE_TYPE_SCHEME_KEY, issueTypeScheme);
        contextMap.add(CONTEXT_ERRORS_KEY, errors);

        return contextMap.toMap();
    }

    private Collection<ProjectIssueType> getSimpleIssueTypes(final Project project)
    {
        final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());

        final List<ProjectIssueType> simpleIssueTypes = Lists.newArrayList();

        for (final IssueType issueType : issueTypes)
        {
            String workflowName = getWorkflow(project, issueType);

            FieldScreenScheme fieldScreenScheme = getIssueTypeScreenScheme(project, issueType);

            FieldLayout fieldLayout = getFieldLayout(project, issueType);

            boolean isDefault = false;
            if (defaultIssueType != null && defaultIssueType.equals(issueType))
            {
                isDefault =  true;
            }
            simpleIssueTypes.add(new ProjectIssueType(issueType, isDefault, workflowName, fieldScreenScheme, fieldLayout));
        }
        Collections.sort(simpleIssueTypes, comparatorFactory.createIssueTypeComparator());
        return simpleIssueTypes;
    }

    private FieldLayout getFieldLayout(Project project, IssueType issueType)
    {
        return fieldLayoutManager.getFieldLayout(project, issueType.getId());
    }

    private FieldScreenScheme getIssueTypeScreenScheme(Project project, IssueType issueType)
    {
        IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project.getGenericValue());
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueType.getId());
        if (issueTypeScreenSchemeEntity == null)
        {
            // Try default entry
            issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null);
            if (issueTypeScreenSchemeEntity == null)
            {
                throw new IllegalStateException("No default entity for issue type screen scheme with id '" + issueTypeScreenScheme.getId() + "'.");
            }
        }
        return issueTypeScreenSchemeEntity.getFieldScreenScheme();
    }

    private String getWorkflow(Project project, IssueType issueType)
    {
        return worlflowSchemeManager.getWorkflowName(project, issueType.getId());
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class ProjectIssueType implements SimpleIssueType
    {
        private final String iconUrl;
        private final String name;
        private final String description;
        private final String id;
        private final boolean subTask;
        private final boolean defaultIssueType;
        private final String workflowName;
        private final FieldScreenScheme fieldScreenScheme;
        private final FieldLayout fieldLayout;
        private final IssueType issueType;

        public ProjectIssueType(final IssueType issueType, boolean defaultIssueType, String workflowName,
                FieldScreenScheme fieldScreenScheme, FieldLayout fieldLayout)
        {
            this.issueType = issueType;
            this.id = issueType.getId();
            this.name = issueType.getNameTranslation();
            this.description = issueType.getDescTranslation();
            this.iconUrl = issueType.getIconUrl();
            this.subTask = issueType.isSubTask();
            this.defaultIssueType = defaultIssueType;
            this.workflowName = workflowName;
            this.fieldScreenScheme = fieldScreenScheme;
            this.fieldLayout = fieldLayout;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public boolean isDefault()
        {
            return isDefaultIssueType();
        }

        public String getDescription()
        {
            return description;
        }

        public String getId()
        {
            return id;
        }

        public boolean isSubTask()
        {
            return subTask;
        }

        public boolean isDefaultIssueType()
        {
            return defaultIssueType;
        }

        public String getWorkflowName()
        {
            return workflowName;
        }

        public FieldScreenScheme getFieldScreenScheme()
        {
            return fieldScreenScheme;
        }

        public FieldLayout getFieldLayout()
        {
            return fieldLayout;
        }

        public IssueType getConstant()
        {
            return issueType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ProjectIssueType that = (ProjectIssueType) o;

            if (subTask != that.subTask) { return false; }
            if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = iconUrl != null ? iconUrl.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (subTask ? 1 : 0);
            return result;
        }
    }
}
