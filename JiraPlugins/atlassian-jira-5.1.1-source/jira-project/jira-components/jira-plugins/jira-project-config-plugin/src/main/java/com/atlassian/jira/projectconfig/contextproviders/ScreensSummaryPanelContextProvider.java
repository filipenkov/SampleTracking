package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Provides context for the screens summary panel, in particular the "screenSchemes" object
 * containing the list of {@link SimpleScreenScheme}s.
 *
 * @since v4.4
 */
public class ScreensSummaryPanelContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_ISSUE_TYPE_SCREEN_SCHEME_KEY = "issueTypeScreenScheme";
    static final String CONTEXT_SCREEN_SCHEMES_KEY = "screenSchemes";

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ContextProviderUtils contextProviderUtils;
    private final TabUrlFactory tabUrlFactory;
    private final ComparatorFactory comparatorFactory;

    public ScreensSummaryPanelContextProvider(final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final ContextProviderUtils contextProviderUtils, final TabUrlFactory tabUrlFactory, ComparatorFactory comparatorFactory)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.contextProviderUtils = contextProviderUtils;
        this.tabUrlFactory = tabUrlFactory;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final Project project = (Project) context.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        // For a valid project, this will always return an IssueTypeScreenSchemeImpl
        final IssueTypeScreenScheme issueTypeScreenScheme =
                issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project.getGenericValue());

        final SimpleIssueTypeScreenScheme simpleIssueTypeScreenScheme =
                new SimpleIssueTypeScreenScheme(issueTypeScreenScheme, getChangeSchemeUrl(project.getId()),
                        getEditSchemeUrl());
        final Set<SimpleScreenScheme> screenSchemes = getScreenSchemes(project, issueTypeScreenScheme);

        return MapBuilder.<String, Object>newBuilder()
                .addAll(context)
                .add(CONTEXT_ISSUE_TYPE_SCREEN_SCHEME_KEY, simpleIssueTypeScreenScheme)
                .add(CONTEXT_SCREEN_SCHEMES_KEY, Lists.newArrayList(screenSchemes.iterator()))
                .toMap();

    }

    private Set<SimpleScreenScheme> getScreenSchemes(final Project project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        final Set<SimpleScreenScheme> operationScreens = Sets.newTreeSet(comparatorFactory.createNamedDefaultComparator());
        final FieldScreenScheme defaultFieldScreenScheme = issueTypeScreenScheme.getEntity(null).getFieldScreenScheme();
        for (final IssueType issueTypes : project.getIssueTypes())
        {
            final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueTypes.getId());

            if(issueTypeScreenSchemeEntity == null || issueTypeScreenSchemeEntity.getFieldScreenScheme().getId().equals(defaultFieldScreenScheme.getId()))
            {
                operationScreens.add(new SimpleScreenScheme(defaultFieldScreenScheme,
                        getScreenSchemeUrl(defaultFieldScreenScheme.getId()), true));
            }
            else
            {
                final FieldScreenScheme fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme();
                operationScreens.add(new SimpleScreenScheme(fieldScreenScheme,
                        getScreenSchemeUrl(fieldScreenScheme.getId()), false));
            }
        }
        return operationScreens;
    }

    String getChangeSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/SelectIssueTypeScreenScheme!default.jspa")
                .addParameter("projectId", id).asUrlString();
    }

    String getEditSchemeUrl()
    {
        return tabUrlFactory.forScreens();
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
        private final String name;
        private final String description;
        private final Long id;
        private final String url;
        private final boolean defaultScreenScheme;

        public SimpleScreenScheme(final FieldScreenScheme fieldScreenScheme, final String url, final boolean defaultScreenScheme)
        {
            this.name = fieldScreenScheme.getName();
            this.description = fieldScreenScheme.getDescription();
            this.id = fieldScreenScheme.getId();
            this.url = url;
            this.defaultScreenScheme = defaultScreenScheme;
        }

        // For unit testing
        SimpleScreenScheme(final Long id, final String name, final String description, final String url, boolean defaultScreenScheme)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.url = url;
            this.defaultScreenScheme = defaultScreenScheme;
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

        public Long getId()
        {
            return id;
        }

        public String getUrl()
        {
            return url;
        }

        public boolean isDefaultScreenScheme()
        {
            return defaultScreenScheme;
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

    public static class SimpleIssueTypeScreenScheme
    {
        private final String name;
        private final String changeUrl;
        private final String editUrl;
        private final String description;

        public SimpleIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme, final String changeUrl, final String editUrl)
        {
            this.name = issueTypeScreenScheme.getName();
            this.description = issueTypeScreenScheme.getDescription();
            this.changeUrl = changeUrl;
            this.editUrl = editUrl;
        }

        SimpleIssueTypeScreenScheme(final String name, final String description, final String changeUrl, final String editUrl)
        {
            this.name = name;
            this.changeUrl = changeUrl;
            this.editUrl = editUrl;
            this.description = description;
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

            if (changeUrl != null ? !changeUrl.equals(that.changeUrl) : that.changeUrl != null) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null)
            {
                return false;
            }
            if (editUrl != null ? !editUrl.equals(that.editUrl) : that.editUrl != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (changeUrl != null ? changeUrl.hashCode() : 0);
            result = 31 * result + (editUrl != null ? editUrl.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            return result;
        }
    }
}
