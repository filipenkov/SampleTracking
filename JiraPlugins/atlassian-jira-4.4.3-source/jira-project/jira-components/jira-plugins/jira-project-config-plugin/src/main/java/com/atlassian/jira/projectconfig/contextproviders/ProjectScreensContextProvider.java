package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context Provider for the notifications panel
 *
 * @since v4.4
 */
public class ProjectScreensContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_ISSUE_TYPE_SCREEN_SCHEME = "issueTypeScreenScheme";
    private static final String CONTEXT_SCREEN_SCHEMES = "screenSchemes";

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ContextProviderUtils contextProviderUtils;
    private final ProjectIssueTypeScreenSchemeHelper helper;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final ComparatorFactory comparatorFactory;

    public ProjectScreensContextProvider(final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final ContextProviderUtils contextProviderUtils, final ProjectIssueTypeScreenSchemeHelper helper,
            IssueTypeSchemeManager issueTypeSchemeManager, final ComparatorFactory comparatorFactory)
    {
        this.contextProviderUtils = contextProviderUtils;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.helper = helper;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {

    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final IssueTypeScreenScheme issueTypeScreenScheme =
                issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project.getGenericValue());

        Long issueTypeScreenSchemeId = issueTypeScreenScheme.getId();
        final SimpleIssueTypeScreenScheme simpleIssueTypeScreenScheme =
                new SimpleIssueTypeScreenScheme(issueTypeScreenScheme, getChangeSchemeUrl(project.getId()), getEditSchemeUrl(issueTypeScreenSchemeId), issueTypeScreenSchemeId);
        final Set<SimpleScreenScheme> screenSchemes = getScreenSchemes(project, issueTypeScreenScheme);

        contextMap.add(CONTEXT_ISSUE_TYPE_SCREEN_SCHEME, simpleIssueTypeScreenScheme);
        contextMap.add(CONTEXT_SCREEN_SCHEMES, screenSchemes);

        return contextMap.toMap();
    }

    private Set<ProjectScreensContextProvider.SimpleScreenScheme> getScreenSchemes(final Project project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        final Set<SimpleScreenScheme> screenSchemes = Sets.newTreeSet(comparatorFactory.createNamedDefaultComparator());
        final FieldScreenScheme defaultFieldScreenScheme = issueTypeScreenScheme.getEntity(null).getFieldScreenScheme();

        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());


        // Get a map of FieldScreenScheme to issue types
        final Map<FieldScreenScheme, List<SimpleIssueType>> fieldScreenMap = new HashMap<FieldScreenScheme, List<SimpleIssueType>>();
        for (final IssueType issueType : project.getIssueTypes())
        {
            final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueType.getId());
            FieldScreenScheme fieldScreenScheme;
            if(issueTypeScreenSchemeEntity == null)
            {
                fieldScreenScheme = defaultFieldScreenScheme;
            }
            else
            {
                fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme();
            }
            List<SimpleIssueType> issueTypeList = fieldScreenMap.get(fieldScreenScheme);
            if (issueTypeList == null)
            {
                issueTypeList = new ArrayList<SimpleIssueType>();
                fieldScreenMap.put(fieldScreenScheme, issueTypeList);
            }
            issueTypeList.add(new SimpleIssueTypeImpl(issueType, issueType.equals(defaultIssueType)));
        }

        final Multimap<FieldScreenScheme, Project> projectsForFieldScreenSchemes = helper.getProjectsForFieldScreenSchemes(fieldScreenMap.keySet());


        for (Map.Entry<FieldScreenScheme, List<SimpleIssueType>> entry : fieldScreenMap.entrySet())
        {
            FieldScreenScheme fieldScreenScheme = entry.getKey();
            List<SimpleIssueType> issueTypeList = entry.getValue();
            Collections.sort(issueTypeList, comparatorFactory.createIssueTypeComparator());

            boolean isDefault = fieldScreenScheme.getId().equals(defaultFieldScreenScheme.getId());

            Collection<ScreenableIssueOperation> operations = IssueOperations.getIssueOperations();

            List<SimpleFieldScreen> screens = new ArrayList<SimpleFieldScreen>();
            for (ScreenableIssueOperation operation : operations)
            {
                FieldScreen fieldScreen = fieldScreenScheme.getFieldScreen(operation);
                screens.add(new SimpleFieldScreen(operation, fieldScreen));
            }

            Collection<Project> projects = projectsForFieldScreenSchemes.get(fieldScreenScheme);
            final SimpleScreenScheme simpleScreenScheme = new SimpleScreenScheme(fieldScreenScheme, getScreenSchemeUrl(fieldScreenScheme.getId()), isDefault, issueTypeList, screens, projects);
            screenSchemes.add(simpleScreenScheme);
        }

        return screenSchemes;
    }

    String getChangeSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/SelectIssueTypeScreenScheme!default.jspa")
                .addParameter("projectId", id).asUrlString();
    }

    String getEditSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/ConfigureIssueTypeScreenScheme.jspa")
                .addParameter("id", id).asUrlString();
    }

    String getScreenSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/ConfigureFieldScreenScheme.jspa")
                .addParameter("id", id).asUrlString();
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return contextProviderUtils.createUrlBuilder(operation);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleScreenScheme implements NamedDefault
    {
        private final FieldScreenScheme fieldScreenScheme;
        private final String name;
        private final String description;
        private final Long id;
        private final String url;
        private final boolean defaultScreenScheme;
        private final List<SimpleIssueType> issueTypes;
        private final List<SimpleFieldScreen> screens;
        private final Collection<Project> schemeProjectList;

        public SimpleScreenScheme(final FieldScreenScheme fieldScreenScheme, final String url, final boolean defaultScreenScheme, List<SimpleIssueType> issueTypes, List<SimpleFieldScreen> screens, Collection<Project> projects)
        {
            this.fieldScreenScheme = fieldScreenScheme;
            this.name = fieldScreenScheme.getName();
            this.description = fieldScreenScheme.getDescription();
            this.id = fieldScreenScheme.getId();
            this.url = url;
            this.defaultScreenScheme = defaultScreenScheme;
            this.issueTypes = issueTypes;
            this.screens = screens;
            this.schemeProjectList = projects;
        }

        public FieldScreenScheme getFieldScreenScheme()
        {
            return fieldScreenScheme;
        }

        public Long getId()
        {
            return id;
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
            return isDefaultScreenScheme();
        }

        public String getUrl()
        {
            return url;
        }

        public boolean isDefaultScreenScheme()
        {
            return defaultScreenScheme;
        }

        public List<SimpleIssueType> getIssueTypes()
        {
            return issueTypes;
        }

        public List<SimpleFieldScreen> getScreens()
        {
            return screens;
        }

        public Collection<Project> getSchemeProjectList()
        {
            return schemeProjectList;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleScreenScheme that = (SimpleScreenScheme) o;

            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
            if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            return result;
        }
    }

    public static class SimpleFieldScreen implements Comparable<SimpleFieldScreen>
    {
        private final ScreenableIssueOperation operation;
        private final FieldScreen fieldScreen;

        public SimpleFieldScreen(ScreenableIssueOperation operation, FieldScreen fieldScreen)
        {
            this.operation = operation;
            this.fieldScreen = fieldScreen;
        }

        public IssueOperation getOperation()
        {
            return operation;
        }

        public FieldScreen getFieldScreen()
        {
            return fieldScreen;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleFieldScreen that = (SimpleFieldScreen) o;

            if (fieldScreen != null ? !fieldScreen.equals(that.fieldScreen) : that.fieldScreen != null)
            {
                return false;
            }
            if (operation != null ? !operation.equals(that.operation) : that.operation != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = operation != null ? operation.hashCode() : 0;
            result = 31 * result + (fieldScreen != null ? fieldScreen.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(SimpleFieldScreen o)
        {
            return operation.getId().compareTo(o.operation.getId());
        }
    }

    public static class SimpleIssueTypeScreenScheme
    {
        private final Long id;
        private final String name;
        private final String changeUrl;
        private final String editUrl;
        private final String description;

        public SimpleIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme, final String changeUrl, final String editUrl, Long id)
        {
            this.id = id;
            this.name = issueTypeScreenScheme.getName();
            this.description = issueTypeScreenScheme.getDescription();
            this.changeUrl = changeUrl;
            this.editUrl = editUrl;
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getChangeUrl()
        {
            return changeUrl;
        }

        public String getEditUrl()
        {
            return editUrl;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleIssueTypeScreenScheme that = (SimpleIssueTypeScreenScheme) o;

            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return id != null ? id.hashCode() : 0;
        }
    }
}
